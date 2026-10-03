"""试卷分析接口

路径保持 /api/ai/paper/analyze 不变。
新增 /difficulty 做考前难度预估，/review 做组卷审查。
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
from app.skills.schemas import PaperDifficultyInput, PaperReviewInput

logger = logging.getLogger(__name__)
router = APIRouter()


class PaperAnalyzeRequest(BaseModel):
    paper_id: str = ""
    title: str | None = None
    duration: int = 60
    pass_score: float = 60
    total_score: float = 100
    questions: list[dict[str, Any]] = Field(default_factory=list)
    tenant_id: str = "000000"
    focus: str = "全面审查"
    extra: dict[str, Any] = Field(default_factory=dict)


@router.post("/analyze")
async def analyze_paper(req: PaperAnalyzeRequest) -> dict:
    """试卷难度与知识点覆盖分析"""
    skill = skill_registry.get("paper_review")
    if skill is None:
        raise AgentError("paper_review Skill 未注册")

    ctx = SkillContext(tenant_id=req.tenant_id)
    inp = PaperReviewInput(
        title=req.title or req.paper_id,
        duration=req.duration,
        pass_score=req.pass_score,
        questions=req.questions,
        focus=req.focus,
    )
    r = await skill.run(inp, ctx)
    return ok(
        {
            "paper_id": req.paper_id,
            "analysis": r.data,
            "model": r.model,
            "prompt_version": r.prompt_version,
            "trace_id": r.trace_id,
        }
    )


@router.post("/review")
async def review_paper(req: PaperAnalyzeRequest) -> dict:
    """组卷审查（覆盖度 / 难度分布 / 重复度 / 预计用时）"""
    return await analyze_paper(req)


@router.post("/difficulty")
async def predict_difficulty(req: PaperAnalyzeRequest) -> dict:
    """考前难度预估：平均分、通过率、分数分布"""
    skill = skill_registry.get("paper_difficulty")
    if skill is None:
        raise AgentError("paper_difficulty Skill 未注册")

    ctx = SkillContext(tenant_id=req.tenant_id)
    inp = PaperDifficultyInput(
        title=req.title or req.paper_id,
        duration=req.duration,
        total_score=req.total_score,
        pass_score=req.pass_score,
        questions=req.questions,
        audience=req.extra.get("audience", "全体考生"),
        history=req.extra.get("history", ""),
    )
    r = await skill.run(inp, ctx)
    return ok({"paper_id": req.paper_id, "prediction": r.data, "model": r.model})
