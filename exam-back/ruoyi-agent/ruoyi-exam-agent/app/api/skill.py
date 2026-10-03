"""Skill 通用接口

一个端点跑所有 Skill：POST /api/ai/skill/{code}/run
新增 Skill 不需要加接口，注册即可用。
"""

from __future__ import annotations

import logging
from typing import Any

from fastapi import APIRouter, Query
from pydantic import BaseModel, Field

from app.core.errors import AgentError, SkillNotFoundError
from app.core.response import ok
from app.skills.base import SkillContext
from app.skills.registry import registry as skill_registry

logger = logging.getLogger(__name__)
router = APIRouter()


class SkillRunRequest(BaseModel):
    tenant_id: str = "000000"
    user_id: str | None = None
    model_code: str | None = None
    trace_id: str | None = None
    input: dict[str, Any] = Field(default_factory=dict)


class BatchItem(BaseModel):
    input: dict[str, Any]
    trace_id: str | None = None


class BatchRequest(BaseModel):
    tenant_id: str = "000000"
    user_id: str | None = None
    model_code: str | None = None
    items: list[BatchItem] = Field(default_factory=list)
    max_parallel: int = Field(default=4, ge=1, le=16)
    skip_error: bool = True


@router.get("/skill/list")
async def list_skills() -> dict:
    """列出所有已注册的 Skill（含输入 schema 与提示词版本）"""
    return ok({"count": skill_registry.size, "items": skill_registry.list_all()})


@router.get("/skill/{code}")
async def skill_detail(code: str) -> dict:
    skill = skill_registry.get(code)
    if skill is None:
        raise SkillNotFoundError(f"未注册的 Skill: {code}")
    return ok(skill.info())


@router.post("/skill/{code}/run")
async def run_skill(code: str, req: SkillRunRequest) -> dict:
    """执行单个 Skill"""
    skill = skill_registry.get(code)
    if skill is None:
        raise SkillNotFoundError(f"未注册的 Skill: {code}")

    ctx = SkillContext(
        tenant_id=req.tenant_id,
        user_id=req.user_id,
        model_code=req.model_code,
        trace_id=req.trace_id or "",
    )
    try:
        inp = skill.input_model(**req.input)
    except Exception as e:  # noqa: BLE001
        raise AgentError(f"Skill {code} 入参不合法: {e}") from e

    result = await skill.run(inp, ctx)
    data = result.data
    if hasattr(data, "model_dump"):
        data = data.model_dump()
    elif isinstance(data, list):
        data = [d.model_dump() if hasattr(d, "model_dump") else d for d in data]

    return ok(
        {
            "skill": code,
            "version": result.version,
            "data": data,
            "need_human": result.need_human,
            "model": result.model,
            "attempt_chain": result.attempt_chain,
            "latency_ms": result.latency_ms,
            "prompt_version": result.prompt_version,
            "trace_id": result.trace_id,
        }
    )


@router.post("/skill/{code}/batch")
async def run_skill_batch(code: str, req: BatchRequest) -> dict:
    """批量执行（如整场考试的主观题预评）

    单条失败不影响其它（skip_error=True 时），
    返回逐条结果，失败的带 error 字段 —— 批量场景不能因为一条挂掉就全盘失败。
    """
    import asyncio

    skill = skill_registry.get(code)
    if skill is None:
        raise SkillNotFoundError(f"未注册的 Skill: {code}")

    sem = asyncio.Semaphore(req.max_parallel)

    async def one(idx: int, item: BatchItem) -> dict[str, Any]:
        async with sem:
            ctx = SkillContext(
                tenant_id=req.tenant_id,
                user_id=req.user_id,
                model_code=req.model_code,
                trace_id=item.trace_id or "",
            )
            try:
                inp = skill.input_model(**item.input)
                r = await skill.run(inp, ctx)
                data = r.data
                if hasattr(data, "model_dump"):
                    data = data.model_dump()
                return {
                    "index": idx,
                    "ok": True,
                    "data": data,
                    "need_human": r.need_human,
                    "model": r.model,
                    "trace_id": r.trace_id,
                    "latency_ms": r.latency_ms,
                }
            except Exception as e:  # noqa: BLE001
                logger.warning("批量执行 %s 第 %d 条失败: %s", code, idx, e)
                if not req.skip_error:
                    raise
                return {"index": idx, "ok": False, "error": str(e)}

    results = await asyncio.gather(*[one(i, it) for i, it in enumerate(req.items)])
    failed = [r for r in results if not r.get("ok")]
    return ok(
        {
            "skill": code,
            "total": len(results),
            "success": len(results) - len(failed),
            "failed": len(failed),
            "results": list(results),
        }
    )
