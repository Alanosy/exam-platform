"""对话接口

Java 网关把它包成 POST /ai/chat 给前端聊天窗调用。

一次请求 = 一轮对话。请求里带 answers 时表示用户在回答上一轮的中断卡。
"""

from __future__ import annotations

import logging
from typing import Any

from fastapi import APIRouter
from pydantic import BaseModel, Field

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
    )
    return ok(result.model_dump())


@router.post("/chat/reset")
async def chat_reset(req: ResetIn) -> dict:
    """清空一个会话（前端「新建会话」按钮）"""
    removed = store.drop(req.session_id) if req.session_id else False
    return ok({"removed": removed, "size": store.size})


@router.get("/chat/session/{session_id}")
async def chat_session(session_id: str) -> dict:
    """查看会话状态，排查「为什么它又问了一遍」这类问题时很有用"""
    session = store.get(session_id)
    if session is None:
        return ok({"exists": False})
    return ok(
        {
            "exists": True,
            "intent": session.intent,
            "slots": session.slots,
            "messages": session.messages[-10:],
        }
    )
