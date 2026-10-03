"""AI 出题接口

路径保持 /api/ai/question/generate 不变，Java 侧无需改调用地址。

新增：
- /generate-audit  出题后立刻自检（Reflection），未通过的题目单独列出
- /rewrite         改写扩量
- /distractor      干扰项生成
- /tag             知识点打标（补 question 表缺失的知识点字段）
- /analysis        解析生成
"""

from __future__ import annotations

import logging
from typing import Any

from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.agent.critic import generate_and_audit
from app.core.errors import AgentError
from app.core.response import ok
from app.skills.base import SkillContext
from app.skills.registry import registry as skill_registry
from app.skills.schemas import (
    AnalysisGenInput,
    DistractorGenInput,
    KnowledgeTagInput,
    QuestionGenInput,
    QuestionRewriteInput,
)

logger = logging.getLogger(__name__)
router = APIRouter()


class QuestionGenerateRequest(BaseModel):
    knowledge_points: list[str] = Field(default_factory=list)
    difficulty: str = "medium"
    question_type: str = "SINGLE"
    count: int = Field(default=5, ge=1, le=30)
    score: float = 5
    material_key: str | None = None
    tenant_id: str = "000000"
    with_audit: bool = Field(default=False, description="是否同时做质检")
    extra: dict[str, Any] = Field(default_factory=dict)


@router.post("/generate")
async def generate_questions(req: QuestionGenerateRequest) -> dict:
    """生成题目（统一返回体 R<T>，data 里是题目数组）"""
    skill = skill_registry.get("question_gen")
    if skill is None:
        raise AgentError("question_gen Skill 未注册")

    ctx = SkillContext(tenant_id=req.tenant_id)
    inp = QuestionGenInput(
        question_type=req.question_type,
        difficulty=req.difficulty,
        knowledge_points=req.knowledge_points,
        count=req.count,
        score=req.score,
        extra=req.extra,
    )

    if req.with_audit:
        return ok(await generate_and_audit("question_gen", inp.model_dump(), ctx))

    result = await skill.run(inp, ctx)
    questions = [q.model_dump() if hasattr(q, "model_dump") else q for q in (result.data or [])]
    return ok(
        {
            "questions": questions,
            "count": len(questions),
            "model": result.model,
            "attempt_chain": result.attempt_chain,
            "prompt_version": result.prompt_version,
            "latency_ms": result.latency_ms,
            "trace_id": result.trace_id,
        }
    )


@router.post("/generate-audit")
async def generate_with_audit(req: QuestionGenerateRequest) -> dict:
    """出题 + 质检（Generate-Critique 循环）

    questions 里是**通过质检的**，未通过的放 rejected 交给人工，
    避免「AI 生成的错题直接进题库」。
    """
    ctx = SkillContext(tenant_id=req.tenant_id)
    return ok(
        await generate_and_audit(
            "question_gen",
            {
                "question_type": req.question_type,
                "difficulty": req.difficulty,
                "knowledge_points": req.knowledge_points,
                "count": req.count,
                "score": req.score,
                "extra": req.extra,
            },
            ctx,
        )
    )


class RewriteRequest(BaseModel):
    question_type: str
    stem: str
    options: list[dict[str, Any]] | None = None
    answer: str | None = None
    strategy: str = "change_scene"
    count: int = Field(default=3, ge=1, le=10)
    tenant_id: str = "000000"


@router.post("/rewrite")
async def rewrite(req: RewriteRequest) -> dict:
    skill = skill_registry.get("question_rewrite")
    if skill is None:
        raise AgentError("question_rewrite Skill 未注册")
    ctx = SkillContext(tenant_id=req.tenant_id)
    inp = QuestionRewriteInput(
        question_type=req.question_type,
        stem=req.stem,
        options=req.options,  # type: ignore[arg-type]
        answer=req.answer,
        strategy=req.strategy,
        count=req.count,
    )
    r = await skill.run(inp, ctx)
    items = [q.model_dump() if hasattr(q, "model_dump") else q for q in (r.data or [])]
    return ok({"questions": items, "count": len(items), "model": r.model})


class DistractorRequest(BaseModel):
    stem: str
    correct_key: str = "A"
    correct_content: str = ""
    existing_options: list[dict[str, Any]] | None = None
    count: int = Field(default=3, ge=1, le=4)
    tenant_id: str = "000000"


@router.post("/distractor")
async def distractor(req: DistractorRequest) -> dict:
    skill = skill_registry.get("distractor_gen")
    if skill is None:
        raise AgentError("distractor_gen Skill 未注册")
    ctx = SkillContext(tenant_id=req.tenant_id)
    inp = DistractorGenInput(
        stem=req.stem,
        correct_key=req.correct_key,
        correct_content=req.correct_content,
        existing_options=req.existing_options,  # type: ignore[arg-type]
        count=req.count,
    )
    r = await skill.run(inp, ctx)
    items = [d.model_dump() if hasattr(d, "model_dump") else d for d in (r.data or [])]
    return ok({"options": items, "model": r.model})


class TagRequest(BaseModel):
    stem: str
    question_type: str = ""
    options: list[dict[str, Any]] | None = None
    answer: str | None = None
    candidates: list[str] | None = None
    tenant_id: str = "000000"


@router.post("/tag")
async def tag(req: TagRequest) -> dict:
    skill = skill_registry.get("knowledge_tag")
    if skill is None:
        raise AgentError("knowledge_tag Skill 未注册")
    ctx = SkillContext(tenant_id=req.tenant_id)
    inp = KnowledgeTagInput(
        stem=req.stem,
        question_type=req.question_type,
        options=req.options,  # type: ignore[arg-type]
        answer=req.answer,
        candidates=req.candidates,
    )
    r = await skill.run(inp, ctx)
    return ok({"data": r.data.model_dump(), "model": r.model})


class AnalysisRequest(BaseModel):
    stem: str
    question_type: str = ""
    options: list[dict[str, Any]] | None = None
    answer: str | None = None
    tenant_id: str = "000000"


@router.post("/analysis")
async def analysis(req: AnalysisRequest) -> dict:
    skill = skill_registry.get("analysis_gen")
    if skill is None:
        raise AgentError("analysis_gen Skill 未注册")
    ctx = SkillContext(tenant_id=req.tenant_id)
    inp = AnalysisGenInput(
        stem=req.stem,
        question_type=req.question_type,
        options=req.options,  # type: ignore[arg-type]
        answer=req.answer,
    )
    r = await skill.run(inp, ctx)
    return ok({"data": r.data.model_dump(), "model": r.model})
