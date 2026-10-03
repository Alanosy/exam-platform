"""ruoyi-exam-agent FastAPI 入口

AI 编排层：Java 网关 ruoyi-exam-ai（9220）通过 REST 调用本服务（9221），
不直接暴露到 Spring Cloud Gateway。

分层：
    L5 接入层   app/api/**
    L4 编排层   app/agent/**      Planner / Executor / Critic / Guardrail / Orchestrator
    L3 能力层   app/skills/**  app/tools/**  app/memory/**  app/rag/**  app/prompts/**
    L2 网关层   app/gateway/**    主备切换 + 熔断 + 审计
    L1 协议层   app/llm/**        OpenAI 兼容协议

启动顺序（lifespan）：
    日志 -> 提示词 -> Skill -> 预热记忆后端 -> 注册 Nacos
任一步失败都不阻塞启动，但会在 /health 里体现出来。
"""

from __future__ import annotations

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware

from app.api import (
    agent,
    grade,
    health,
    model,
    paper,
    proctor,
    question,
    rag,
    recommend,
    skill,
    tool,
)
from app.config import settings
from app.core import nacos
from app.core.errors import AgentError
from app.core.logging import setup_logging
from app.core.response import CODE_ERROR, json_fail
from app.prompts.registry import registry as prompt_registry
from app.skills.registry import registry as skill_registry

logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    setup_logging()
    logger.info("ruoyi-exam-agent 启动中... port=%s", settings.port)

    # L3：提示词与 Skill 必须先加载，否则所有接口都不可用
    prompt_count = prompt_registry.load()
    skill_count = skill_registry.load()
    logger.info("提示词 %d 条 / Skill %d 个", prompt_count, skill_count)

    # L3：预热记忆后端（Redis 不可用时这里会降级为内存）
    try:
        from app.memory.store import memory

        _ = memory.backend
    except Exception as e:  # noqa: BLE001
        logger.warning("记忆层初始化异常: %s", e)

    # L5：注册到 Nacos（失败不阻塞，以未注册状态继续运行）
    await nacos.register_to_nacos()

    logger.info("ruoyi-exam-agent 就绪: http://0.0.0.0:%s/docs", settings.port)
    yield
    await nacos.shutdown_nacos()
    logger.info("ruoyi-exam-agent 已停止")


app = FastAPI(
    title="ruoyi-exam-agent",
    description="考试系统 AI 编排服务：Skill / Tool / Agent / 模型网关",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.exception_handler(AgentError)
async def agent_error_handler(request: Request, exc: AgentError) -> object:
    """业务异常统一转成 R<T>，HTTP 仍为 200（与 RuoYi 一致）"""
    return json_fail(msg=exc.message, code=exc.code, data=exc.data)


@app.exception_handler(Exception)
async def unhandled_handler(request: Request, exc: Exception) -> object:
    logger.exception("未处理异常: %s", exc)
    return json_fail(msg=f"服务内部错误: {exc}", code=CODE_ERROR)


# ---------------- L5 路由 ----------------
API = "/api/ai"

# 健康检查放根路径：Dockerfile HEALTHCHECK 与 Nacos 探活都打 /health
app.include_router(health.router)
app.include_router(health.router, prefix=API, include_in_schema=False)
app.include_router(question.router, prefix=f"{API}/question", tags=["AI 出题"])
app.include_router(grade.router, prefix=f"{API}/grade", tags=["AI 阅卷"])
app.include_router(recommend.router, prefix=f"{API}/recommend", tags=["AI 学情"])
app.include_router(paper.router, prefix=f"{API}/paper", tags=["AI 试卷"])
app.include_router(proctor.router, prefix=f"{API}/proctor", tags=["AI 监考"])
app.include_router(skill.router, prefix=API, tags=["Skill"])
app.include_router(tool.router, prefix=API, tags=["Tool"])
app.include_router(agent.router, prefix=API, tags=["Agent 编排"])
app.include_router(model.router, prefix=API, tags=["模型网关"])
app.include_router(rag.router, prefix=API, tags=["RAG"])


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "app.main:app",
        host=settings.host,
        port=settings.port,
        reload=settings.debug,
        log_level=settings.log_level.lower(),
    )
