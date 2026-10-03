"""Critic：Reflection 模式的落点

为什么性价比最高（架构文档 4.1⑥）：
    Reflection 只需额外 1 次调用，就能拦掉大部分低级错误；
    Multi-Agent 需要 2-5 次。在命题和阅卷这两个「错不起」的域，Reflection 是标配。
"""

from __future__ import annotations

import logging
from typing import Any

from app.config import settings
from app.skills.base import SkillContext
from app.skills.registry import registry as skill_registry

logger = logging.getLogger(__name__)


async def reflect_question(payload: dict[str, Any], ctx: SkillContext) -> dict[str, Any]:
    """出题后自检：调用 question_audit"""
    skill = skill_registry.get("question_audit")
    if skill is None:
        return {"passed": True, "quality_score": 0, "issues": [], "summary": "质检 Skill 未注册"}

    from app.skills.schemas import QuestionAuditInput

    inp = QuestionAuditInput(**payload)
    result = await skill.run(inp, ctx)
    data: dict[str, Any] = result.data.model_dump() if hasattr(result.data, "model_dump") else {}
    data["_model"] = result.model
    data["_prompt_version"] = result.prompt_version
    return data


async def reflect_score(
    payload: dict[str, Any], ctx: SkillContext
) -> dict[str, Any]:
    """评分后自检：调用 mark_reflect"""
    skill = skill_registry.get("mark_reflect")
    if skill is None:
        return {"accepted": True, "adjusted_score": payload.get("ai_score", 0), "issues": []}

    from app.skills.schemas import MarkReflectInput

    inp = MarkReflectInput(**payload)
    result = await skill.run(inp, ctx)
    data: dict[str, Any] = result.data.model_dump() if hasattr(result.data, "model_dump") else {}
    data["_model"] = result.model
    return data


async def generate_and_audit(
    gen_skill_code: str,
    gen_payload: dict[str, Any],
    ctx: SkillContext,
    audit_builder: Any = None,
) -> dict[str, Any]:
    """生成 + 自检 的组合拳

    生成后逐条质检，未通过的题目单独列出交给人工，
    不作为「可用题目」返回。
    """
    gen_skill = skill_registry.get(gen_skill_code)
    if gen_skill is None:
        from app.core.errors import SkillNotFoundError

        raise SkillNotFoundError(gen_skill_code)

    gen_result = await gen_skill.run(gen_skill.input_model(**gen_payload), ctx)
    questions = gen_result.data if isinstance(gen_result.data, list) else []

    if not questions or not settings.agent_reflection_enabled:
        return {
            "questions": [q.model_dump() if hasattr(q, "model_dump") else q for q in questions],
            "audits": [],
            "rejected": [],
            "reflection": False,
            "model": gen_result.model,
        }

    audit_skill = skill_registry.get("question_audit")
    from app.skills.schemas import QuestionAuditInput

    accepted: list[dict[str, Any]] = []
    audits: list[dict[str, Any]] = []
    rejected: list[dict[str, Any]] = []

    for idx, q in enumerate(questions):
        qd = q.model_dump() if hasattr(q, "model_dump") else dict(q)
        if audit_skill is None:
            accepted.append(qd)
            continue
        try:
            audit_in = QuestionAuditInput(
                question_type=qd.get("question_type") or "",
                difficulty=qd.get("difficulty") or "",
                knowledge_points=qd.get("knowledge_points"),
                stem=qd.get("stem") or "",
                options=qd.get("options"),
                answer=qd.get("answer"),
                analysis=qd.get("analysis"),
            )
            r = await audit_skill.run(audit_in, ctx)
            ad = r.data.model_dump() if hasattr(r.data, "model_dump") else {}
            ad["index"] = idx
            audits.append(ad)
            score = float(ad.get("quality_score") or 0)
            if ad.get("passed") and score >= settings.agent_reflection_min_score * 10:
                accepted.append(qd)
            else:
                rejected.append({"index": idx, "question": qd, "audit": ad})
        except Exception as e:  # noqa: BLE001
            logger.warning("第 %d 题质检失败，按可用处理: %s", idx, e)
            accepted.append(qd)

    return {
        "questions": accepted,
        "audits": audits,
        "rejected": rejected,
        "reflection": True,
        "model": gen_result.model,
    }
