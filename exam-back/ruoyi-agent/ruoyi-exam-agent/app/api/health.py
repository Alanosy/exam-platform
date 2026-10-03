"""健康检查

给三处用：
1. Nacos 探活（Dockerfile HEALTHCHECK 也打这里）
2. 运维一眼看清「服务活着 + 模型通不通 + 注册没注册」
3. Java 网关判断是否要把流量打过来

注意：健康检查**不代表模型可用**。模型挂了这里仍是 UP，
但会带 model.status=down，避免把「服务活着」误当成「AI 能用」。
"""

from __future__ import annotations

from fastapi import APIRouter

from app.core import nacos
from app.core.logging import call_log
from app.gateway.router import router as model_router
from app.prompts.registry import registry as prompt_registry
from app.rag.retriever import rag
from app.skills.registry import registry as skill_registry

router = APIRouter(tags=["健康检查"])


@router.get("/health")
async def health() -> dict:
    return {
        "status": "UP",
        "service": "ruoyi-exam-agent",
        "version": "1.0.0",
        "nacos": nacos.nacos_state(),
        "skills": skill_registry.size,
        "prompts": prompt_registry.size,
        "rag": rag.stats(),
        "model": model_router.health(),
        "call": call_log.stats(),
    }


@router.get("/ping")
async def ping() -> dict:
    """最轻量探活，不查任何依赖"""
    return {"pong": True}
