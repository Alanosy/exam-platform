"""学情诊断与个性化推题

路径保持 /api/ai/recommend/wrong 不变。

新增 /diagnose：先归因再推题（Memory-Augmented + ReAct 的典型用法）。
长期记忆（学情画像）由 app.memory.store 提供，Redis 不可用时降级为内存。
"""

from __future__ import annotations

import logging
from typing import Any

from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.core.errors import AgentError
from app.core.response import ok
from app.memory.store import memory
from app.skills.base import SkillContext
from app.skills.registry import registry as skill_registry
from app.skills.schemas import DiagnosisInput, RecommendInput

logger = logging.getLogger(__name__)
router = APIRouter()


class RecommendRequest(BaseModel):
    user_id: str
    wrong_question_ids: list[str] = Field(default_factory=list)
    mastered_points: dict[str, float] = Field(default_factory=dict)
    weak_points: list[str] | None = None
    candidates: list[dict[str, Any]] | None = None
    target_count: int = Field(default=5, ge=1, le=10)
    tenant_id: str = "000000"
    extra: dict[str, Any] = Field(default_factory=dict)


@router.post("/wrong")
async def recommend_for_wrong(req: RecommendRequest) -> dict:
    """基于错题与掌握度推荐练习题"""
    skill = skill_registry.get("recommend")
    if skill is None:
        raise AgentError("recommend Skill 未注册")

    ctx = SkillContext(tenant_id=req.tenant_id, user_id=req.user_id)
    inp = RecommendInput(
        profile={"mastered_points": req.mastered_points, "user_id": req.user_id},
        weak_points=req.weak_points,
        candidates=req.candidates or [],
        target_count=req.target_count,
    )
    r = await skill.run(inp, ctx)
    items = [i.model_dump() if hasattr(i, "model_dump") else i for i in (r.data or [])]
    return ok({"items": items, "count": len(items), "model": r.model, "trace_id": r.trace_id})


class DiagnoseRequest(BaseModel):
    user_id: str
    tenant_id: str = "000000"
    wrong_items: list[dict[str, Any]] = Field(default_factory=list)
    with_recommend: bool = True
    candidates: list[dict[str, Any]] | None = None
    target_count: int = Field(default=5, ge=1, le=10)


@router.post("/diagnose")
async def diagnose(req: DiagnoseRequest) -> dict:
    """错题归因（+ 可选推题）

    会把结论写进长期记忆，下次诊断能看到历史薄弱点趋势。
    """
    ctx = SkillContext(tenant_id=req.tenant_id, user_id=req.user_id)

    diag_skill = skill_registry.get("diagnosis")
    if diag_skill is None:
        raise AgentError("diagnosis Skill 未注册")

    inp = DiagnosisInput(
        wrong_items=req.wrong_items,
        mastered=await memory.profile(req.tenant_id, req.user_id) or None,
    )
    r = await diag_skill.run(inp, ctx)
    data = r.data if isinstance(r.data, dict) else {}

    # 结论写入长期记忆
    if data.get("weak_points"):
        await memory.update_profile(req.tenant_id, req.user_id, "weak_points", data["weak_points"])
    if data.get("priority"):
        await memory.update_profile(req.tenant_id, req.user_id, "priority", data["priority"])

    result: dict[str, Any] = {
        "diagnosis": data,
        "model": r.model,
        "trace_id": r.trace_id,
    }

    if req.with_recommend:
        rec_skill = skill_registry.get("recommend")
        if rec_skill is not None:
            weak = [w.get("knowledge_point") for w in data.get("weak_points", []) if isinstance(w, dict)]
            rin = RecommendInput(
                profile={"user_id": req.user_id},
                weak_points=weak,
                candidates=req.candidates or [],
                target_count=req.target_count,
            )
            rr = await rec_skill.run(rin, ctx)
            result["recommend"] = [
                i.model_dump() if hasattr(i, "model_dump") else i for i in (rr.data or [])
            ]

    return ok(result)


@router.get("/profile/{user_id}")
async def get_profile(user_id: str, tenant_id: str = "000000") -> dict:
    """读取考生的长期学情画像"""
    return ok(await memory.profile(tenant_id, user_id))
