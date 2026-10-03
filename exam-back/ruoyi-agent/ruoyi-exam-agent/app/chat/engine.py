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
import re
from typing import Any

from app.chat import flows
from app.chat.models import ChatResult, TraceStep
from app.chat.planner import identity_audience
from app.chat.session import store
from app.chat.slots import INTENT_CHAT, detect_intent, extract_slots, merge_slots
from app.skills.base import SkillContext

logger = logging.getLogger(__name__)

# 只有管理 / 教师侧能跑的预设流程，考生问了就交给规划器按他的权限重新判断
ADMIN_ONLY_INTENTS = {"question_create", "exam_analysis"}

# 一句话打发掉中断卡的说法
CANCEL_WORDS = {"取消", "算了", "不用了", "不了", "别了", "取消吧", "cancel", "no"}
# 确认卡上用户手打的肯定答复
CONFIRM_WORDS = {"确认", "确定", "可以", "好的", "好的。", "执行", "继续", "是的", "对", "yes", "ok", "嗯", "行"}
# 中断卡问的字段，用户可能直接写「count=10」这种形式
_KV_PAT = re.compile(r"([\w]+)\s*[:：=]\s*([^\s,，]+)")


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

    waiting = bool(session.pending_fields)
    if answers:
        # 中断续跑：答案并入槽位，沿用原来的意图
        intent = session.intent or "chat"
        slots = merge_slots(session.slots, answers)
        session.add_message("user", "（补充信息）" + _answers_text(answers))
        # 续跑时这条消息是「补充信息」，原始问题还留在槽位里，不能拿它覆盖，
        # 否则后续汇总/规划拿到的问题是空的
        question = str(session.slots.get("__question") or message)
    elif waiting and (guessed := _guess_answers(message, session.pending_fields, ask_kind=session.pending_kind)) is not None:
        # 用户没点表单、直接在输入框里回答（最常见就是手打一个「1」）。
        # 这时必须接着上一轮的意图往下跑，否则「上下文没连上」——
        # 手打的答案会被当成新话题，走一遍通用规划然后答非所问。
        intent = session.intent or "chat"
        slots = merge_slots(session.slots, extract_slots(message, intent))
        slots = merge_slots(slots, guessed)
        session.add_message("user", message)
        question = str(session.slots.get("__question") or message)
        if guessed.get("__cancel__"):
            session.intent = ""
            session.slots = {}
            session.pending_fields = []
            return ChatResult(session_id=session.id, reply="已取消，需要的时候再找我就行。", trace=trace, intent=intent)
    else:
        session.add_message("user", message)
        intent = detect_intent(message)
        if intent != session.intent:
            # 换意图了：上一轮没跑完的槽位作废，避免拿旧题库去干新活
            session.slots = {}
        elif intent == INTENT_CHAT and waiting and session.intent:
            # 正在等补充信息时，任何没有独立意图的话都算作答/追问，
            # 不能开成新话题，否则上一轮攒下的槽位全白费
            intent = session.intent
        slots = merge_slots(session.slots, extract_slots(message, intent))
        question = message

    slots["__question"] = question
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
        session.pending_kind = ask.kind
        session.pending_fields = [
            {
                "key": f.key,
                "type": f.type,
                "options": [{"label": o.get("label", ""), "value": o.get("value", "")} for o in (f.options or [])],
            }
            for f in (ask.fields or [])
        ]
        if ask.kind == "confirm":
            # 确认卡手打「确认」也要能过，插入一个虚拟字段参与猜测
            session.pending_fields.insert(0, {"key": "confirmed", "type": "confirm"})
        session.add_message("assistant", ask.title)
        trace.append(TraceStep(type="ask", title="等待用户补充信息", detail=ask.title, status="waiting"))
        return ChatResult(session_id=session.id, reply="", trace=trace, ask=ask, intent=intent, data=data)

    # 跑完：清空中断态
    session.intent = ""
    session.slots = {}
    session.pending_fields = []
    session.pending_kind = ""
    session.add_message("assistant", reply or "")
    return ChatResult(session_id=session.id, reply=reply or "", trace=trace, intent=intent, data=data)


def _coerce(field: dict[str, Any], text: str) -> Any:
    """把用户手打的一句话塞回某个字段；塞不进去返回 None（宁可不猜，不要猜错）"""
    raw = (text or "").strip()
    if not raw:
        return None
    ftype = field.get("type")
    if ftype == "number":
        m = re.search(r"\d+", raw)
        return int(m.group()) if m else None
    if ftype == "select":
        options = field.get("options") or []
        hit = next((o for o in options if str(o.get("value")) == raw), None)
        if hit is None:
            hit = next((o for o in options if raw and str(o.get("label", "")).startswith(raw)), None)
        return str(hit["value"]) if hit else None
    return raw


def _guess_answers(
    message: str, fields: list[dict[str, Any]], ask_kind: str = ""
) -> dict[str, Any] | None:
    """判断一句手打的话是不是在回答上一张中断卡

    返回 None 表示「不像在回答」，按新提问处理。
    只在会话处于中断态时调用，所以这里默认用户是在作答。
    """
    text = (message or "").strip()
    if not text:
        return None
    lowered = text.lower().rstrip("。！!")

    if lowered in CANCEL_WORDS:
        return {"__cancel__": True}

    # 确认卡优先：用户手打「确认」「执行」等同于点了确认按钮
    if ask_kind == "confirm" or any(f.get("type") == "confirm" for f in fields):
        if lowered in CONFIRM_WORDS or any(w in text for w in ("确认", "执行", "写进去", "入库")):
            return {"confirmed": True}

    # 明显是新指令（又一句话里带着「出题」「答题情况」这种强特征）就别当成答案
    if detect_intent(text) != INTENT_CHAT:
        return None

    plain = [f for f in fields if f.get("type") != "confirm"]
    if len(plain) == 1:
        value = _coerce(plain[0], text)
        return {plain[0]["key"]: value} if value is not None else None

    # 多个字段时：一句「1」只可能是在回答那个数字框
    numbers = [f for f in plain if f.get("type") == "number"]
    if len(numbers) == 1 and re.fullmatch(r"\d+\s*(?:道|个|条)?", text):
        return {numbers[0]["key"]: int(re.search(r"\d+", text).group())}

    # 整句像「计算机基础题库」：以「题库」收尾就是库名，否则只认唯一一个文本框
    texts = [f for f in plain if f.get("type") == "text"]
    if text.endswith("题库") and any(f.get("key") == "bank_name" for f in texts):
        return {"bank_name": text}
    if len(texts) == 1:
        value = _coerce(texts[0], text)
        if value is not None:
            return {texts[0]["key"]: value}

    # 兜底只认「字段名=值」的写法，猜错了比不猜更糟
    out: dict[str, Any] = {}
    for key, raw in _KV_PAT.findall(text):
        field = next((f for f in plain if f.get("key") == key), None)
        if field is None:
            continue
        value = _coerce(field, raw)
        if value is not None:
            out[key] = value
    return out or None


def _answers_text(answers: dict[str, Any]) -> str:
    """答案转一行文本，用于会话历史。选项类字段只记 key，避免历史过长"""
    parts = []
    for key, value in answers.items():
        if isinstance(value, (list, dict)):
            value = str(value)[:60]
        parts.append(f"{key}={value}")
    return " " + ", ".join(parts)
