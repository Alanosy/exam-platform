"""对话引擎

一轮对话的完整生命周期：

    用户消息 -> 意图识别 -> 槽位抽取 -> 跑 flow
                                          ├─ 缺信息：返回 AskForm，状态存进会话（中断）
                                          └─ 跑完：返回 Markdown 回复，清空中断态

中断后用户提交答案时，engine 会**带着累积的槽位重跑 flow**，
所以 flow 必须是幂等的：已确定的东西（题库 ID、考试 ID）存在 slots 里，
重跑时直接跳过对应的匹配步骤。
"""

from __future__ import annotations

import logging
from typing import Any

from app.chat import flows
from app.chat.models import ChatResult, TraceStep
from app.chat.session import store
from app.chat.slots import detect_intent, extract_slots, merge_slots
from app.skills.base import SkillContext

logger = logging.getLogger(__name__)


async def chat(
    message: str,
    session_id: str | None = None,
    answers: dict[str, Any] | None = None,
    tenant_id: str = "000000",
    user_id: str | None = None,
    model_code: str | None = None,
) -> ChatResult:
    """处理一轮对话"""
    session = store.get_or_create(session_id, tenant_id=tenant_id, user_id=user_id)
    trace: list[TraceStep] = []

    if answers:
        # 中断续跑：答案并入槽位，沿用原来的意图
        intent = session.intent or "chat"
        slots = merge_slots(session.slots, answers)
        session.add_message("user", "（补充信息）" + _answers_text(answers))
    else:
        session.add_message("user", message)
        intent = detect_intent(message)
        if intent != session.intent:
            # 换意图了：上一轮没跑完的槽位作废，避免拿旧题库去干新活
            session.slots = {}
        slots = merge_slots(session.slots, extract_slots(message, intent))

    slots["__question"] = message
    slots["__history"] = session.history_text(limit=6)

    ctx = SkillContext(tenant_id=tenant_id, user_id=user_id, model_code=model_code)

    flow = flows.FLOWS.get(intent, flows.flow_chat)
    try:
        reply, ask, data = await flow(slots, ctx, trace)
    except Exception as e:  # noqa: BLE001
        logger.exception("对话流程 %s 执行异常", intent)
        return ChatResult(
            session_id=session.id,
            reply=f"执行过程中出错了：{e}",
            trace=trace,
            intent=intent,
        )

    if ask is not None:
        # 中断：把状态写回会话，等用户回答
        session.intent = intent
        session.slots = slots
        session.add_message("assistant", ask.title)
        trace.append(TraceStep(type="ask", title="等待用户补充信息", detail=ask.title, status="waiting"))
        return ChatResult(session_id=session.id, reply="", trace=trace, ask=ask, intent=intent, data=data)

    # 跑完：清空中断态
    session.intent = ""
    session.slots = {}
    session.add_message("assistant", reply or "")
    return ChatResult(session_id=session.id, reply=reply or "", trace=trace, intent=intent, data=data)


def _answers_text(answers: dict[str, Any]) -> str:
    """答案转一行文本，用于会话历史。选项类字段只记 key，避免历史过长"""
    parts = []
    for key, value in answers.items():
        if isinstance(value, (list, dict)):
            value = str(value)[:60]
        parts.append(f"{key}={value}")
    return " " + ", ".join(parts)
