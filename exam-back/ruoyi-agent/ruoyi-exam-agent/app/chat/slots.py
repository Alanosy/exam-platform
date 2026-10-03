"""意图识别与槽位抽取

两段式：
1. **规则优先**。出题、查答题情况这类高频指令有极强的措辞特征，
   正则一抓一个准，还不花 token、不受模型波动影响。
2. **LLM 兜底**。规则没命中时才问模型，拿到 intent + slots 的 JSON。

为什么不直接全交给 LLM：模型在「10 道」这种数字上偶发丢字、
在题库名上会自由发挥（「计算机基础题库」被改写成「计算机题库」），
而规则抽出来的槽位是**原样字符串**，后面做模糊匹配时这一点很关键。
"""

from __future__ import annotations

import logging
import re
from typing import Any

logger = logging.getLogger(__name__)

INTENT_QUESTION_CREATE = "question_create"
INTENT_EXAM_ANALYSIS = "exam_analysis"
INTENT_QUESTION_SEARCH = "question_search"
INTENT_CHAT = "chat"

# 题型中文 -> code
TYPE_MAP = {
    "单选": "SINGLE", "单选题": "SINGLE", "选择": "SINGLE", "选择题": "SINGLE",
    "多选": "MULTIPLE", "多选题": "MULTIPLE",
    "判断": "JUDGE", "判断题": "JUDGE", "判断题": "JUDGE",
    "填空": "BLANK", "填空题": "BLANK",
    "简答": "SHORT_ANSWER", "简答题": "SHORT_ANSWER",
    "论述": "ESSAY", "论述题": "ESSAY",
    "编程": "CODE", "代码": "CODE",
}

# 注意：这里**刻意不放**「基础」「难」这类词。
# 「计算机基础知识」是主题不是难度，「难题」也不是难度，放进来会误判。
DIFFICULTY_MAP = {
    "简单": "easy", "容易": "easy", "入门": "easy", "低难度": "easy",
    "中等": "medium", "一般": "medium", "适中": "medium", "中难度": "medium",
    "困难": "hard", "较难": "hard", "高难": "hard", "高难度": "hard",
}

STATUS_MAP = {"草稿": "0", "启用": "1", "发布": "1", "废弃": "2"}

# 意图关键词
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

# 只认「数字 + 量词」：「加3道简单题」里数字和题之间还隔着难度词，
# 要求后面紧跟「题」会漏抽
_COUNT_PAT = re.compile(r"(\d+)\s*(?:道|个|条)")
_BANK_PAT = re.compile(r"([一-龥A-Za-z0-9]+?)\s*题库")
_TOPIC_PAT = re.compile(r"关于(.+?)(?:的题|的题目|的试题|的知识点|方面|的)")
_EXAM_PAT = re.compile(r"([一-龥A-Za-z0-9]+?)\s*(?:考卷|试卷|考试)")
_TYPE_PAT = re.compile(r"(" + "|".join(sorted(TYPE_MAP, key=len, reverse=True)) + r")")
_DIFF_PAT = re.compile(r"(" + "|".join(sorted(DIFFICULTY_MAP, key=len, reverse=True)) + r")")
_STATUS_PAT = re.compile(r"(草稿|启用|发布|废弃)")


def detect_intent(text: str) -> str:
    """规则识别意图，识别不出返回 chat"""
    if _CREATE_PAT.search(text):
        return INTENT_QUESTION_CREATE
    if _ANALYSIS_PAT.search(text):
        return INTENT_EXAM_ANALYSIS
    if _SEARCH_PAT.search(text):
        return INTENT_QUESTION_SEARCH
    return INTENT_CHAT


def extract_slots(text: str, intent: str) -> dict[str, Any]:
    """从一句话里抽槽位，抽不到的一律不填（让流程去问，不要瞎猜）"""
    slots: dict[str, Any] = {}

    m = _COUNT_PAT.search(text)
    if m:
        slots["count"] = int(m.group(1))

    m = _BANK_PAT.search(text)
    if m:
        cleaned = _clean_bank_keyword(m.group(1))
        if cleaned:
            slots["bank_keyword"] = cleaned

    m = _TOPIC_PAT.search(text)
    if m:
        slots["topic"] = m.group(1).strip()

    m = _EXAM_PAT.search(text)
    if m:
        slots["exam_keyword"] = m.group(1).strip()

    m = _TYPE_PAT.search(text)
    if m:
        slots["question_type"] = TYPE_MAP.get(m.group(1), "")

    m = _DIFF_PAT.search(text)
    if m:
        slots["difficulty"] = DIFFICULTY_MAP.get(m.group(1), "")

    m = _STATUS_PAT.search(text)
    if m:
        slots["status"] = STATUS_MAP.get(m.group(1), "0")

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
    # 再砍一道：清洗后仍然过长说明抽得不对，交给调用方用主题派生
    return name if 0 < len(name) <= 8 else ""


def _topic_to_bank_keyword(topic: str) -> str:
    """「计算机基础知识」-> 「计算机」，题库名通常比主题短

    取前 2~4 个字做关键词：太短（「数学」）匹配面还行，
    太长（「计算机基础知识」）在模糊匹配里几乎必空。
    """
    topic = re.sub(r"(基础|知识|相关|方面|相关知识点|题库)$", "", topic).strip()
    if not topic:
        return ""
    return topic[:4] if len(topic) > 4 else topic[:2]


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
