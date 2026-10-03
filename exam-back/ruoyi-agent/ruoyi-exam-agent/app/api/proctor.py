"""监考分析接口

硬约束：**AI 不认定作弊**。
输出的是「可疑程度 + 证据链」，是否认定由人工监考员决定。
接口返回的 need_human 恒为 true。
"""

from __future__ import annotations

import logging
from typing import Any

from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.core.errors import AgentError
from app.core.response import ok
from app.skills.base import SkillContext
from app.skills.registry import registry as skill_registry
from app.skills.schemas import ProctorAnalyzeInput, ReportWriteInput

logger = logging.getLogger(__name__)
router = APIRouter()


class ProctorAnalyzeRequest(BaseModel):
    exam_id: str = ""
    user_id: str = ""
    user_name: str = ""
    exam_name: str = ""
    duration: int = 60
    events: list[dict[str, Any]] = Field(default_factory=list)
    thresholds: dict[str, Any] | None = None
    tenant_id: str = "000000"
    with_report: bool = False


@router.post("/analyze")
async def analyze(req: ProctorAnalyzeRequest) -> dict:
    """分析监考事件时序，输出证据链"""
    skill = skill_registry.get("proctor_analyze")
    if skill is None:
        raise AgentError("proctor_analyze Skill 未注册")

    ctx = SkillContext(tenant_id=req.tenant_id, user_id=req.user_id)
    inp = ProctorAnalyzeInput(
        user_name=req.user_name,
        exam_name=req.exam_name,
        duration=req.duration,
        events=req.events,
        thresholds=req.thresholds,
    )
    r = await skill.run(inp, ctx)
    data = r.data if isinstance(r.data, dict) else {}
    data["need_human"] = True  # 强制：作弊认定必须人工

    out: dict[str, Any] = {
        "exam_id": req.exam_id,
        "user_id": req.user_id,
        "result": data,
        "model": r.model,
        "trace_id": r.trace_id,
    }

    if req.with_report:
        report_skill = skill_registry.get("report_write")
        if report_skill is not None:
            rin = ReportWriteInput(
                subject=f"{req.exam_name} 监考情况分析",
                audience="监考员",
                stats=data,
            )
            rr = await report_skill.run(rin, ctx)
            out["report"] = rr.data

    return ok(out)
