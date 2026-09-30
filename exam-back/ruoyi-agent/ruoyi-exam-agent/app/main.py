"""ruoyi-exam-agent FastAPI 入口

作为 AI 编排层，由 Java 网关 ruoyi-exam-ai（9220）通过 REST 调用，
本身不直接暴露到 Spring Cloud Gateway。

能力：
- /api/ai/question/generate   AI 出题
- /api/ai/grade/auto          AI 自动阅卷（主观题评分）
- /api/ai/recommend           错题推荐 / 个性化推题
- /api/ai/paper/analyze       试卷难度与知识点覆盖分析
- /health                     健康检查
"""

from __future__ import annotations

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.api import grade, paper, question, recommend
from app.config import settings
from app.core.nacos import register_to_nacos, shutdown_nacos

logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期：启动时注册到 Nacos，关闭时注销。"""
    if settings.nacos_enabled:
        await register_to_nacos()
    yield
    if settings.nacos_enabled:
        await shutdown_nacos()


app = FastAPI(
    title="ruoyi-exam-agent",
    description="RuoYi 考试系统 AI 编排服务（LangChain + LLM）",
    version="1.0.0",
    lifespan=lifespan,
)

app.include_router(question.router, prefix="/api/ai/question", tags=["AI 出题"])
app.include_router(grade.router, prefix="/api/ai/grade", tags=["AI 阅卷"])
app.include_router(recommend.router, prefix="/api/ai/recommend", tags=["AI 推荐"])
app.include_router(paper.router, prefix="/api/ai/paper", tags=["AI 试卷分析"])


@app.get("/health", tags=["健康检查"])
async def health() -> dict:
    return {"status": "UP", "service": "ruoyi-exam-agent", "version": "1.0.0"}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "app.main:app",
        host=settings.host,
        port=settings.port,
        reload=settings.debug,
        log_level="info",
    )
