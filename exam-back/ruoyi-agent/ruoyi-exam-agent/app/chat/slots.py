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
# 中文数字同样要认：「帮我创建一道题」里的「一道」是数量，
# 只认阿拉伯数字会把它当成没写数量，转头去问用户（非常招人烦）
_CN_COUNT_PAT = re.compile(r"([一二两三四五六七八九十]+)\s*(?:道|个|条)")
_CN_NUM = {"一": 1, "二": 2, "两": 2, "三": 3, "四": 4, "五": 5, "六": 6, "七": 7, "八": 8, "九": 9}
_BANK_PAT = re.compile(r"([一-龥A-Za-z0-9]+?)\s*题库")
# 允许「关于X的Y题」中 Y 是修饰词（如「英语阅读理解」），
# 用 [^的]+ 锁定 X 到第一个「的」为止，避免把修饰词吃进主题
_TOPIC_PAT = re.compile(r"(?:关于)?([^的]+?)(?:的|相关的).*?(?:题|题目|试题|单选题|多选题|判断题|填空题|简答题|知识点)")
_EXAM_PAT = re.compile(r"([一-龥A-Za-z0-9]+?)\s*(?:考卷|试卷|考试)")
_TYPE_PAT = re.compile(r"(" + "|".join(sorted(TYPE_MAP, key=len, reverse=True)) + r")")
_DIFF_PAT = re.compile(r"(" + "|".join(sorted(DIFFICULTY_MAP, key=len, reverse=True)) + r")")
_STATUS_PAT = re.compile(r"(草稿|启用|发布|废弃)")

# 语言：出题时按用户指定的语言生成题干和选项
_LANG_MAP = {
    "英语": "en", "英文": "en", "English": "en", "english": "en",
    "中文": "zh", "汉语": "zh", "语文": "zh", "chinese": "zh",
    "数学": "math",
}
_LANG_PAT = re.compile(r"(" + "|".join(sorted(_LANG_MAP, key=len, reverse=True)) + r")")

# 「阅读理解」不是系统支持的独立题型（系统只有单选/多选/判断…），
# 但用户提了就要认：把它作为出题风格指令放进 extra，让模型按「篇章+题目」的形式出
_READING_PAT = re.compile(r"(阅读理解|阅读题|完形填空)")


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
    else:
        m = _CN_COUNT_PAT.search(text)
        count = _cn_to_int(m.group(1)) if m else None
        if count:
            slots["count"] = count

    m = _BANK_PAT.search(text)
    if m:
        cleaned = _clean_bank_keyword(m.group(1))
        if cleaned:
            slots["bank_keyword"] = cleaned

    # 提取 topic 前先剥掉「创建/出/帮我 + 数量 + 道」这类前缀，
    # 否则「创建1道数据结构的单选题」会把 topic 抽成「创建1道数据结构」
    topic_text = re.sub(r"^(?:帮我|我|请|麻烦)?\s*(?:创建|生成|出|写|命)?\s*(?:\d+|[一二两三四五六七八九十]+)?\s*(?:道|个|条)?\s*", "", text)
    m = _TOPIC_PAT.search(topic_text)
    if m:
        topic = m.group(1).strip()
        # 再剥一次残留的数量词，兜底
        topic = re.sub(r"^(?:\d+|[一二两三四五六七八九十]+)?\s*(?:道|个|条)?\s*", "", topic)
        if topic:
            slots["topic"] = topic

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

    # 语言：用户说「英语题」「中文题」时，按指定语言生成
    m = _LANG_PAT.search(text)
    if m:
        slots["language"] = _LANG_MAP.get(m.group(1), "")

    # 阅读理解：系统没有独立的阅读理解题型，作为出题风格标记
    if _READING_PAT.search(text):
        slots["reading_comprehension"] = True

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
