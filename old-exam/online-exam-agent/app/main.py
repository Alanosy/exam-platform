"""FastAPI 应用入口：注册路由、CORS、启动加载知识库。"""
import logging

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.config import settings
from app.routers import diagnosis, grading, qa, question
from app.schemas.common import ApiError, Result

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def create_app() -> FastAPI:
    app = FastAPI(title="online-exam-agent", description="考试系统 AI Agent 微服务")

    # CORS（前端经 vue.config.js 代理 /agent-api 到本服务，仍允许直连）
    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=False,
        allow_methods=["*"],
        allow_headers=["*"],
        expose_headers=["Authorization"],
    )

    app.include_router(question.router, prefix="/api/agent/question", tags=["智能出题Agent"])
    app.include_router(grading.router, prefix="/api/agent/grading", tags=["主观题批改Agent"])
    app.include_router(diagnosis.router, prefix="/api/agent/diagnosis", tags=["学情诊断Agent"])
    app.include_router(qa.router, prefix="/api/agent/qa", tags=["考试答疑Agent"])

    # 统一异常处理：将业务异常/未知异常转换为考试系统一致的 Result 结构
    @app.exception_handler(ApiError)
    async def _handle_api_error(_: Request, exc: ApiError):
        return JSONResponse(status_code=200, content=Result.failed(exc.detail).model_dump())

    @app.exception_handler(Exception)
    async def _handle_exception(_: Request, exc: Exception):
        logger.exception("agent 服务异常")
        return JSONResponse(status_code=200, content=Result.failed(f"服务异常: {exc}").model_dump())

    @app.on_event("startup")
    def _startup() -> None:
        # 懒加载知识库：后台线程预热，避免启动期阻塞
        from app.services.knowledge_base import knowledge_base

        knowledge_base.warmup()
        logger.info("online-exam-agent 启动完成，监听 %s:%s", settings.server_host, settings.server_port)

    return app


app = create_app()

