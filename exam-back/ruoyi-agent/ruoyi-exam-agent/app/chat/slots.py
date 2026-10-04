"""意图识别与槽位抽取

设计原则：**语义理解优先，正则只做兜底**。
- 意图：正则快判（出题/查分/搜题特征极强），LLM 不参与
- 槽位：交给 LLM 语义理解，不维护一堆脆弱的正则映射表
- 数量：正则兜底抽数字（纯数字 LLM 偶发丢字，正则更稳）
- LLM 失败时：退化到极简正则，保证流程不崩

为什么不全靠 LLM：
- 「10 道」这种纯数字，正则比 LLM 更准更快
- 意图判断关键词特征极强，没必要花 token
- 但 topic / 题型 / 难度 / 语言 / 阅读理解这些**语义参数**，
  正则维护成本高、容易漏，交给 LLM 语义理解更灵活
"""

from __future__ import annotations

import logging
import re
from typing import Any

from app.gateway.models import ChatMessage, ChatRequest
from app.gateway.router import router
from app.llm.structured import parse_model
from app.prompts.registry import registry as prompt_registry
from app.skills.base import SkillContext
from app.skills.schemas import SlotExtractOutput

logger = logging.getLogger(__name__)

INTENT_QUESTION_CREATE = "question_create"
INTENT_EXAM_ANALYSIS = "exam_analysis"
INTENT_QUESTION_SEARCH = "question_search"
INTENT_CHAT = "chat"

# 意图关键词（只做意图快判，不做槽位抽取）
_CREATE_PAT = re.compile(
    r"(出\s*\d*\s*[道个条]*题|生成.*?题|创建.*?题|新建.*?题|编\s*\d*\s*[道个条]*题|"
    r"加\s*\d+\s*道|来\s*\d+\s*道|写\s*\d+\s*道|帮我出|造\s*\d+\s*道|"
    r"出\s*\d+\s*道|要\s*\d+\s*道)"
)
_ANALYSIS_PAT = re.compile(
    r"(答题情况|答题怎么样|考得怎么样|考的怎么样|成绩怎么样|成绩如何|"
    r"通过率|及格率|平均分|得分情况|作答情况|考情)"
)
_SEARCH_PAT = re.compile(r"(搜\s*\d*\s*[道个条]*题|找\s*\d+\s*道|查\s*\d+\s*道|有没有.*?题|检索.*?题)")

# 数量：纯数字用正则兜底，比 LLM 稳
_COUNT_PAT = re.compile(r"(\d+)\s*(?:道|个|条)")
_CN_COUNT_PAT = re.compile(r"([一二两三四五六七八九十]+)\s*(?:道|个|条)")
_CN_NUM = {"一": 1, "二": 2, "两": 2, "三": 3, "四": 4, "五": 5, "六": 6, "七": 7, "八": 8, "九": 9}

# 题库关键词兜底正则（LLM 失败时用）
_BANK_PAT = re.compile(r"([一-龥A-Za-z0-9]+?)\s*题库")


def detect_intent(text: str) -> str:
    """规则识别意图，识别不出返回 chat"""
    if _CREATE_PAT.search(text):
        return INTENT_QUESTION_CREATE
    if _ANALYSIS_PAT.search(text):
        return INTENT_EXAM_ANALYSIS
    if _SEARCH_PAT.search(text):
        return INTENT_QUESTION_SEARCH
    return INTENT_CHAT


async def extract_slots(
    text: str, intent: str, ctx: SkillContext | None = None
) -> dict[str, Any]:
    """从一句话里抽槽位，LLM 语义理解为主，正则兜底

    返回的 slots 里只填有把握的字段，没把握的不填，让流程去问用户。
    如果 LLM 判断出的意图和正则不一致（正则说 chat 但 LLM 说出题），
    会把 LLM 的意图写进 slots["__intent__"]，调用方据此覆盖。
    """
    slots: dict[str, Any] = {}

    # 口语量词归一化：俩=两个(2)、仨=三个(3)，补出量词让正则也能命中
    text = text.replace("俩", "两个").replace("仨", "三个")

    # 1. 数量：正则先抽，纯数字 LLM 偶发丢字
    m = _COUNT_PAT.search(text)
    if m:
        slots["count"] = int(m.group(1))
    else:
        m = _CN_COUNT_PAT.search(text)
        count = _cn_to_int(m.group(1)) if m else None
        if count:
            slots["count"] = count

    # 2. 语义参数：交给 LLM 理解
    llm_slots, llm_intent = await _llm_extract(text, ctx)
    if llm_slots:
        # LLM 抽到的覆盖（count：正则没抽到才用 LLM 的——纯数字正则更准，但
        # 「整俩」「来几道」这种没量词的口语只能靠 LLM 理解）
        for k, v in llm_slots.items():
            if k == "count" and slots.get("count"):
                continue
            if v not in (None, "", [], False):
                slots[k] = v

    # 3. 兜底：LLM 没抽到题库名时，正则试一下
    if not slots.get("bank_keyword"):
        m = _BANK_PAT.search(text)
        if m:
            cleaned = _clean_bank_keyword(m.group(1))
            if cleaned:
                slots["bank_keyword"] = cleaned

    # 4. 意图覆盖：正则判成 chat 但 LLM 认出是业务意图时，听 LLM 的
    if intent == INTENT_CHAT and llm_intent in (
        INTENT_QUESTION_CREATE, INTENT_EXAM_ANALYSIS, INTENT_QUESTION_SEARCH
    ):
        slots["__intent__"] = llm_intent
        intent = llm_intent

    # 出题场景：没写出题库名时，用主题当题库关键词去模糊匹配
    if intent == INTENT_QUESTION_CREATE:
        if not slots.get("topic") and slots.get("bank_keyword"):
            slots["topic"] = slots["bank_keyword"]
        if not slots.get("bank_keyword") and slots.get("topic"):
            slots["bank_keyword"] = _topic_to_bank_keyword(slots["topic"])

    # 检索场景：用户说「关于 X 的题」时 X 落在 topic 上，这里转成检索关键词
    if intent == INTENT_QUESTION_SEARCH and not slots.get("keyword"):
        slots["keyword"] = slots.get("topic") or slots.get("bank_keyword") or ""

    return slots


async def _llm_extract(
    text: str, ctx: SkillContext | None
) -> tuple[dict[str, Any], str]:
    """用 LLM 语义理解抽取槽位，失败返回 (空dict, "")"""
    try:
        tpl = prompt_registry.get("slot_extract")
        if tpl is None:
            logger.warning("slot_extract 提示词未注册，跳过 LLM 抽取")
            return {}, ""

        system, user = tpl.render(message=text)
        params = tpl.params or {}
        messages = []
        if system:
            messages.append(ChatMessage(role="system", content=system))
        messages.append(ChatMessage(role="user", content=user))

        req = ChatRequest(
            messages=messages,
            temperature=params.get("temperature", 0.01),
            max_tokens=params.get("max_tokens", 600),
            response_json=True,
            response_shape="object",
            model_code=ctx.model_code if ctx else None,
        )
        tenant_id = ctx.tenant_id if ctx else "000000"
        resp = await router.chat(req, tenant_id=tenant_id, biz_type="slot_extract")

        out = parse_model(resp.content or "{}", SlotExtractOutput)
        result: dict[str, Any] = {}
        for field in (
            "topic", "question_type", "difficulty", "bank_keyword",
            "exam_keyword", "keyword", "language", "status",
        ):
            val = getattr(out, field, None)
            if val:
                result[field] = val
        if getattr(out, "count", None):
            result["count"] = out.count
        if getattr(out, "reading_comprehension", False):
            result["reading_comprehension"] = True
        return result, out.intent or ""
    except Exception as e:  # noqa: BLE001
        logger.warning("LLM 槽位抽取失败，退化到正则: %s", e)
        return {}, ""


# 「XX题库」前面那一串往往是「帮我创建10道关于……的题到」，
# 直接取 group 会把整句都带进来，这里按常见切分点砍掉，只留紧贴题库名的那一段
_BANK_SPLIT = re.compile(r"(?:到|入|进|加入|存入|写入|放到|放进|关于|道|个|条|的题|的题目|这些|上述)")
_BANK_PREFIX = re.compile(
    r"^(?:帮我|请|麻烦|给我|然后|并且|创建|生成|新建|加入|存入|写入|放到|放进|加到|把|出题|加|写|造|来|出|在|从|往)+"
)


def _clean_bank_keyword(raw: str) -> str:
    """从「帮我创建10道关于X的题到计算机」里洗出「计算机」"""
    parts = _BANK_SPLIT.split(raw)
    name = next((p for p in reversed(parts) if p.strip()), raw)
    name = _BANK_PREFIX.sub("", name.strip())
    return name if 0 < len(name) <= 8 else ""


def _topic_to_bank_keyword(topic: str) -> str:
    """「计算机基础知识」-> 「计算机」：题库名通常比主题短

    只是**兜底用**的派生关键词，主关键词仍是完整主题。
    别对短词动刀：「计算机」截成「计算」在模糊匹配里反而更差。
    """
    topic = re.sub(r"(基础|知识|相关|方面|相关知识点|题库)$", "", topic).strip()
    if not topic:
        return ""
    return topic if len(topic) <= 6 else topic[:4]


def _cn_to_int(text: str) -> int | None:
    """「一」-> 1，「十二」-> 12，「二十」-> 20；解析不了返回 None"""
    if not text:
        return None
    if text.isdigit():
        return int(text)
    if "十" in text:
        left, _, right = text.partition("十")
        tens = _CN_NUM.get(left, 1) if left else 1
        ones = _CN_NUM.get(right, 0) if right else 0
        return tens * 10 + ones
    return _CN_NUM.get(text)


def merge_slots(base: dict[str, Any], incoming: dict[str, Any]) -> dict[str, Any]:
    """合并槽位：新值覆盖旧值，空值不覆盖

    为什么要单独写：answers 里用户可能只改了题型，
    直接 dict.update 会用 None 把已有的主题冲掉。
    """
    merged = dict(base)
    for key, value in (incoming or {}).items():
        if value is None or value == "" or value == []:
            continue
        merged[key] = value
    return merged
