"""Agent 编排接口

两类：
1. /agent/plan/*   —— 预置计划（Plan-and-Execute）的查看与执行
2. /agent/orchestrate/* —— Multi-Agent 拓扑（pipeline / vote / panel）
"""

from __future__ import annotations

import logging
from typing import Any

from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.agent.critic import generate_and_audit, reflect_question, reflect_score
from app.agent.executor import execute_plan
from app.agent.guardrail import guardrail_policy
from app.agent.orchestrator import panel, pipeline, vote
from app.agent.planner import list_plans
from app.core.errors import AgentError
from app.core.response import ok
from app.skills.base import SkillContext

logger = logging.getLogger(__name__)
router = APIRouter()


class PlanRunRequest(BaseModel):
    tenant_id: str = "000000"
    user_id: str | None = None
    model_code: str | None = None
    params: dict[str, Any] = Field(default_factory=dict)
    approved: bool = Field(default=False, description="允许写操作（高危需人工批准）")
    max_steps: int | None = None
    stop_on_error: bool = True


class OrchestrateRequest(BaseModel):
    tenant_id: str = "000000"
    user_id: str | None = None
    model_code: str | None = None
    skills: list[str] = Field(default_factory=list)
    payload: dict[str, Any] = Field(default_factory=dict)
    tolerance: float = Field(default=1.0, description="投票模式下允许的最大分差")


class ReflectRequest(BaseModel):
    tenant_id: str = "000000"
    kind: str = Field(default="question", description="question / score")
    payload: dict[str, Any] = Field(default_factory=dict)


class GenAuditRequest(BaseModel):
    tenant_id: str = "000000"
    skill: str = Field(default="question_gen")
    payload: dict[str, Any] = Field(default_factory=dict)


@router.get("/agent/plan/list")
async def plans() -> dict:
    return ok({"items": list_plans(), "guardrail": guardrail_policy()})


@router.post("/agent/plan/{task_type}/run")
async def run_plan(task_type: str, req: PlanRunRequest) -> dict:
    """执行一个预置计划"""
    ctx = SkillContext(
        tenant_id=req.tenant_id, user_id=req.user_id, model_code=req.model_code
    )
    result = await execute_plan(
        task_type,
        req.params,
        ctx,
        approved=req.approved,
        max_steps=req.max_steps,
        stop_on_error=req.stop_on_error,
    )
    return ok(
        {
            "task_type": result.task_type,
            "ok": result.ok,
            "need_human": result.need_human,
            "error": result.error,
            "latency_ms": result.latency_ms,
            "steps": [
                {
                    "id": s.id, "name": s.name, "kind": s.kind, "ref": s.ref,
                    "ok": s.ok, "latency_ms": s.latency_ms, "error": s.error,
                }
                for s in result.steps
            ],
            "data": result.data,
        }
    )


@router.post("/agent/orchestrate/pipeline")
async def orch_pipeline(req: OrchestrateRequest) -> dict:
    ctx = SkillContext(tenant_id=req.tenant_id, user_id=req.user_id, model_code=req.model_code)
    return ok(await pipeline(req.skills, req.payload, ctx))


@router.post("/agent/orchestrate/vote")
async def orch_vote(req: OrchestrateRequest) -> dict:
    if len(req.skills) < 2:
        raise AgentError("投票模式至少需要 2 个 Agent", code=400)
    ctx = SkillContext(tenant_id=req.tenant_id, user_id=req.user_id, model_code=req.model_code)
    return ok(await vote(req.skills, req.payload, ctx, tolerance=req.tolerance))


@router.post("/agent/orchestrate/panel")
async def orch_panel(req: OrchestrateRequest) -> dict:
    ctx = SkillContext(tenant_id=req.tenant_id, user_id=req.user_id, model_code=req.model_code)
    return ok(await panel(req.skills, req.payload, ctx))


@router.post("/agent/reflect")
async def reflect(req: ReflectRequest) -> dict:
    """单次 Reflection 自检"""
    ctx = SkillContext(tenant_id=req.tenant_id)
    if req.kind == "score":
        return ok(await reflect_score(req.payload, ctx))
    if req.kind == "question":
        return ok(await reflect_question(req.payload, ctx))
    raise AgentError(f"未知的 reflect kind: {req.kind}", code=400)


@router.post("/agent/generate-and-audit")
async def gen_audit(req: GenAuditRequest) -> dict:
    """出题 + 质检 的组合拳（最常用的 Generate-Critique 循环）"""
    ctx = SkillContext(tenant_id=req.tenant_id)
    return ok(await generate_and_audit(req.skill, req.payload, ctx))
