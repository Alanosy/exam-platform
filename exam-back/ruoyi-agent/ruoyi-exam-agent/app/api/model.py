"""模型网关运维接口

暴露主备链、熔断状态、调用流水，让「主模型挂了多久、切了几次」是可见的。
"""

from __future__ import annotations

import logging

from fastapi import APIRouter, Query

from app.core.logging import call_log
from app.core.response import ok
from app.gateway.providers import registry as model_registry
from app.gateway.router import router as model_router

logger = logging.getLogger(__name__)
router = APIRouter()


@router.get("/model/list")
async def list_models(tenant_id: str = "000000", refresh: bool = False) -> dict:
    """列出当前生效的模型配置（api_key 已脱敏），按优先级排序"""
    cfgs = await model_router.list_models(tenant_id, force=refresh)
    return ok(
        {
            "tenant_id": tenant_id,
            "count": len(cfgs),
            "primary": cfgs[0].name if cfgs else None,
            "standby": [c.name for c in cfgs[1:]],
            "models": [c.public_dict() for c in cfgs],
        }
    )


@router.get("/model/health")
async def model_health(tenant_id: str = "000000") -> dict:
    """网关健康快照：熔断状态 + 配置源 + 调用统计"""
    return ok(model_router.health(tenant_id))


@router.post("/model/reload")
async def reload_models(tenant_id: str | None = None) -> dict:
    """强制刷新模型配置缓存（后台改了 ai_model_config 后调用）"""
    model_registry.invalidate(tenant_id)
    cfgs = await model_router.list_models(tenant_id or "000000", force=True)
    return ok({"reloaded": True, "count": len(cfgs), "names": [c.name for c in cfgs]})


@router.post("/model/circuit/reset")
async def reset_circuit(model_code: str | None = None) -> dict:
    """手动复位熔断（主模型恢复了但还在冷却时用）"""
    model_router.circuit.reset(model_code)
    return ok({"reset": True, "model_code": model_code or "(全部)"})


@router.get("/model/call-log")
async def get_call_log(limit: int = Query(default=50, ge=1, le=500)) -> dict:
    """最近的 AI 调用流水（含尝试链，可看出是否发生过切换）"""
    return ok({"stats": call_log.stats(), "items": call_log.recent(limit)})
