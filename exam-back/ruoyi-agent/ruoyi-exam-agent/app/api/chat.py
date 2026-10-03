"""对话接口

Java 网关把它包成 POST /ai/chat 给前端聊天窗调用。

一次请求 = 一轮对话。请求里带 answers 时表示用户在回答上一轮的中断卡。
"""

from __future__ import annotations

import logging
from typing import Any

from fastapi import APIRouter
from pydantic import BaseModel, Field, field_validator

from app.chat.engine import chat
from app.chat.session import store
from app.core.response import ok

logger = logging.getLogger(__name__)
router = APIRouter()


class ChatIn(BaseModel):
    """一轮对话请求"""

    session_id: str | None = None
    message: str = Field(default="", description="用户消息；回答中断卡时可为空")
    answers: dict[str, Any] | None = Field(default=None, description="中断卡的答案 {key: value}")
    tenant_id: str = "000000"
    user_id: str | None = None
    model_code: str | None = None
    # 当前登录用户的令牌：Agent 调业务接口时**以这个用户的身份**去调，
    # 权限判定交给网关与业务服务的 @SaCheckPermission，Agent 不做自己的一套判断
    token: str | None = None
    # 网关校验「客户端ID与Token匹配」要用，和令牌一起带上
    client_id: str | None = None
    # 身份快照（角色 + 考试域权限），由 Java 侧从登录态里取，不信客户端上报
    identity: dict[str, Any] | None = None
    # 会话设置：上下文轮数 / 写操作确认 / 模型，见 session.DEFAULT_OPTIONS
    options: dict[str, Any] | None = None

    @field_validator("message", mode="before")
    @classmethod
    def _blank_message(cls, value: Any) -> Any:
        """回答中断卡时前端只回 answers，message 会传 null"""
        return value if isinstance(value, str) else ("" if value is None else str(value))

    @field_validator("tenant_id", "user_id", "model_code", mode="before")
    @classmethod
    def _to_str(cls, value: Any) -> Any:
        """ID 一律收成字符串

        Java 侧的 userId 是 Long、租户号也可能是数字，直接按 str 校验会 422，
        而 422 在 Java 那边只会显示成一句「AI 服务暂时不可用」，很难排查。
        雪花 ID 本来也该全程当字符串传，这里统一兜一层。
        """
        return None if value is None else str(value)


class ResetIn(BaseModel):
    session_id: str = ""


@router.post("/chat")
async def chat_turn(req: ChatIn) -> dict:
    """跑一轮对话，返回 Markdown 回复 + 运行流程 +（可能的）中断卡"""
    result = await chat(
        message=req.message,
        session_id=req.session_id,
        answers=req.answers,
        tenant_id=req.tenant_id,
        user_id=req.user_id,
        model_code=req.model_code,
        token=req.token,
        options=req.options,
        client_id=req.client_id,
        identity=req.identity,
    )
    return ok(result.model_dump())


@router.post("/chat/reset")
async def chat_reset(req: ResetIn) -> dict:
    """清空一个会话（前端「新建会话」按钮）"""
    removed = store.drop(req.session_id) if req.session_id else False
    return ok({"removed": removed, "size": store.size})


@router.get("/chat/sessions")
async def chat_sessions(tenant_id: str = "000000", user_id: str | None = None, limit: int = 30) -> dict:
    """历史会话列表（前端「历史会话」面板）"""
    return ok({"items": store.list_sessions(tenant_id=tenant_id, user_id=user_id, limit=limit)})


@router.get("/chat/session/{session_id}")
async def chat_session(session_id: str) -> dict:
    """会话详情：排查「为什么它又问了一遍」的调试视图 + 前端「继续这个会话」的数据源"""
    session = store.get(session_id)
    if session is None:
        return ok({"exists": False})
    return ok(
        {
            "exists": True,
            "id": session.id,
            "title": session.title,
            "intent": session.intent,
            "slots": session.slots,
            "options": session.options,
            "messages": session.messages,
        }
    )
