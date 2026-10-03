"""AI 阅卷接口

路径保持与旧版一致（/api/ai/grade/auto），Java 侧 ruoyi-exam-mark 无需改调用地址。

关键变化：
- 内部改为走 Skill（mark_score），享受主备切换 + 结构化输出 + 置信度门槛
- 返回 need_human：置信度不足时明确要求转人工，**不给假分数**
- 支持 reflection=true 打开评分自检（多一次调用换一致性）
- 支持批量，单条失败不影响其它
"""

from __future__ import annotations

import asyncio
import logging
from typing import Any

from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.core.response import ok
from app.skills.base import SkillContext
from app.skills.mark import CONFIDENCE_FLOOR
from app.skills.registry import registry as skill_registry
from app.skills.schemas import MarkScoreInput

logger = logging.getLogger(__name__)
router = APIRouter()


class GradeItem(BaseModel):
    """单题阅卷请求"""

    question_id: str = ""
    item_id: str = ""
    question_type: str = Field(default="SHORT_ANSWER")
    stem: str
    reference_answer: str = ""
    rubric: list[str] | None = None
    student_answer: str
    full_score: float = Field(default=10, gt=0)
    analysis: str = ""
    anchor_high: str = ""
    anchor_low: str = ""
    extra: dict[str, Any] = Field(default_factory=dict)


class GradeRequest(BaseModel):
    paper_id: str = ""
    answer_sheet_id: str = ""
    record_id: str = ""
    tenant_id: str = "000000"
    reflection: bool = Field(default=False, description="是否启用评分自检")
    max_parallel: int = Field(default=4, ge=1, le=16)
    items: list[GradeItem]


@router.post("/auto")
async def auto_grade(req: GradeRequest) -> dict:
    """AI 批量预评（返回建议分，不给最终分）"""
    skill = skill_registry.get("mark_score")
    if skill is None:
        raise RuntimeError("mark_score Skill 未注册")

    reflect_skill = skill_registry.get("mark_reflect")
    sem = asyncio.Semaphore(req.max_parallel)

    async def one(idx: int, item: GradeItem) -> dict[str, Any]:
        async with sem:
            ctx = SkillContext(tenant_id=req.tenant_id, trace_id=item.item_id or item.question_id)
            inp = MarkScoreInput(
                question_type=item.question_type,
                stem=item.stem,
                full_score=item.full_score,
                standard_answer=item.reference_answer,
                rubric="\n".join(item.rubric or []),
                analysis=item.analysis,
                answer_text=item.student_answer,
                anchor_high=item.anchor_high,
                anchor_low=item.anchor_low,
            )
            try:
                r = await skill.run(inp, ctx)
                data = r.data.model_dump()
            except Exception as e:  # noqa: BLE001
                logger.warning("第 %d 题预评失败: %s", idx, e)
                return {
                    "question_id": item.question_id,
                    "item_id": item.item_id,
                    "ok": False,
                    "error": str(e),
                    "need_human": True,
                }

            out: dict[str, Any] = {
                "question_id": item.question_id,
                "item_id": item.item_id,
                "ok": True,
                "score": data.get("score"),
                "full_score": item.full_score,
                "confidence": data.get("confidence"),
                "matched_points": data.get("matched_points"),
                "reason": data.get("reason"),
                "comment": data.get("comment"),
                "need_human": bool(data.get("need_human")),
                "model": r.model,
                "prompt_version": r.prompt_version,
                "trace_id": r.trace_id,
                "latency_ms": r.latency_ms,
            }

            # Reflection：对已给出的评分做一次自检
            if req.reflection and reflect_skill is not None and out["ok"]:
                try:
                    from app.skills.schemas import MarkReflectInput

                    rin = MarkReflectInput(
                        question_type=item.question_type,
                        stem=item.stem,
                        full_score=item.full_score,
                        standard_answer=item.reference_answer,
                        answer_text=item.student_answer,
                        ai_score=float(data.get("score") or 0),
                        ai_reason=data.get("reason") or "",
                    )
                    rr = await reflect_skill.run(rin, ctx)
                    rd = rr.data.model_dump()
                    out["reflection"] = rd
                    if not rd.get("accepted"):
                        out["need_human"] = True
                        out["score"] = rd.get("adjusted_score", out["score"])
                    out["score"] = max(
                        0.0, min(float(out["score"] or 0), float(item.full_score))
                    )
                except Exception as e:  # noqa: BLE001
                    logger.warning("第 %d 题评分自检失败: %s", idx, e)

            return out

    results = await asyncio.gather(*[one(i, it) for i, it in enumerate(req.items)])
    need_human = [r for r in results if r.get("need_human")]
    failed = [r for r in results if not r.get("ok")]

    return ok(
        {
            "paper_id": req.paper_id,
            "answer_sheet_id": req.answer_sheet_id,
            "record_id": req.record_id,
            "total": len(results),
            "failed": len(failed),
            "need_human_count": len(need_human),
            "confidence_floor": CONFIDENCE_FLOOR,
            "reflection": req.reflection,
            "results": list(results),
            "hint": "score 为 AI 建议分，最终分必须由教师确认",
        }
    )


@router.post("/single")
async def grade_single(item: GradeItem, tenant_id: str = "000000") -> dict:
    """单题预评（便于调试与前端单题触发）"""
    req = GradeRequest(tenant_id=tenant_id, items=[item])
    data = await auto_grade(req)
    return data
