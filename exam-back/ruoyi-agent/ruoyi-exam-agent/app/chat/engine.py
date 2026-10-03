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
from app.chat.planner import identity_audience
from app.chat.session import store
from app.chat.slots import detect_intent, extract_slots, merge_slots
from app.skills.base import SkillContext

logger = logging.getLogger(__name__)

# 只有管理 / 教师侧能跑的预设流程，考生问了就交给规划器按他的权限重新判断
ADMIN_ONLY_INTENTS = {"question_create", "exam_analysis"}


async def chat(
    message: str,
    session_id: str | None = None,
    answers: dict[str, Any] | None = None,
    tenant_id: str = "000000",
    user_id: str | None = None,
    model_code: str | None = None,
    token: str | None = None,
    options: dict[str, Any] | None = None,
    client_id: str | None = None,
    identity: dict[str, Any] | None = None,
) -> ChatResult:
    """处理一轮对话"""
    session = store.get_or_create(session_id, tenant_id=tenant_id, user_id=user_id)
    session.apply_options(options)
    if token:
        # 用户令牌每轮刷新：Agent 之后调业务接口都以这个身份去调，
        # 权限由网关和业务服务的注解判定，Agent 不自己判断「这个人能不能干」
        session.token = token
    if client_id:
        session.client_id = client_id
    if identity:
        session.identity = identity
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
    slots["__history"] = session.history_text()
    # 会话设置里的模型优先于请求参数：设置面板改了就一直生效
    if not model_code:
        model_code = session.option("model_code") or None

    ctx = SkillContext(
        tenant_id=tenant_id,
        user_id=user_id,
        model_code=model_code,
        extra={
            "token": session.token,
            "client_id": session.client_id,
            "identity": session.identity,
            "session_id": session.id,
            "confirm_write": bool(session.option("confirm_write", True)),
        },
    )

    flow = flows.FLOWS.get(intent, flows.flow_general)
    audience = identity_audience(session.identity)
    if audience == "student" and intent in ADMIN_ONLY_INTENTS:
        # 考生问「出题」「全班答题情况」：预置流程是管理端视角，
        # 交给规划器按他的身份重新判断，它会给出「考生只能看自己的数据」这类解释
        flow = flows.flow_general
    if intent in ("chat", "question_search") and session.option("planner", True):
        # 开放域问题 / 检索一律先规划：预设流程覆盖不到的需求才不会直接回一句「做不到」
        flow = flows.flow_general
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
