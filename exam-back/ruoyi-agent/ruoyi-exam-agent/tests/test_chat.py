"""对话式 Agent 单测

验证的是「编排行为」，不是模型效果：
- 缺信息时会不会中断问人（而不是硬着头皮瞎生成）
- 唯一匹配时会不会自动选中（而不是多此一举地问）
- 多个匹配时会不会让人选
- **写库前一定会先确认**（这条最重要，AI 直接往题库里塞数据是事故）
- 工具失败时会不会优雅降级而不是整轮崩掉

工具与技能全部 monkeypatch 掉，不依赖 Java 网关与真实模型。
"""

from __future__ import annotations

from typing import Any

import pytest

from app.chat import flows
from app.chat.engine import chat
from app.chat.models import TraceStep
from app.chat.slots import detect_intent, extract_slots

FAKE_BANK = {"id": "1001", "name": "计算机基础", "questionCount": 12}
FAKE_QUESTION = {
    "question_type": "SINGLE",
    "stem": "CPU 的全称是？",
    "options": [{"key": "A", "content": "中央处理器"}, {"key": "B", "content": "内存"}],
    "answer": '{"rightKeys": ["A"]}',
    "analysis": "CPU = Central Processing Unit",
    "difficulty": "medium",
    "score": 5,
}


def _tool(responses: dict[str, Any]):
    """按 code 返回预设结果的假工具"""

    async def fake(code: str, payload: dict[str, Any], tenant_id: str = "000000", title: str = "") -> tuple[Any, TraceStep]:
        if code not in responses:
            return None, TraceStep(type="tool", ref=code, status="error", title=f"{code} 未 mock")
        return responses[code], TraceStep(type="tool", ref=code, status="ok", title=title or code)

    return fake


def _skill(data: Any):
    async def fake(code: str, payload: dict[str, Any], ctx, title: str = "") -> tuple[Any, TraceStep]:
        return data, TraceStep(type="skill", ref=code, status="ok", title=title or code)

    return fake


# ---------------------------------------------------------------- 槽位


def test_slots_for_question_create():
    text = "帮我创建10道关于计算机基础知识的题到计算机题库"
    slots = extract_slots(text, detect_intent(text))
    assert slots["count"] == 10
    assert slots["topic"] == "计算机基础知识"
    assert slots["bank_keyword"] == "计算机"


def test_slots_for_exam_analysis():
    text = "期中考试考卷的答题情况怎么样"
    assert detect_intent(text) == "exam_analysis"
    assert extract_slots(text, "exam_analysis")["exam_keyword"] == "期中"


def test_basic_words_are_not_difficulty():
    """「基础」「难」这类词不能当难度，否则主题里带它们就会误判"""
    text = "出5道关于计算机基础的题"
    slots = extract_slots(text, "question_create")
    assert "difficulty" not in slots


# ---------------------------------------------------------------- 出题流程


async def test_ask_when_slots_missing(monkeypatch):
    """缺数量/主题/题型/难度 -> 一次性问完，不是问一句等一句"""
    monkeypatch.setattr(flows, "call_tool", _tool({}))
    # 只卡数量与主题：题型 / 难度有默认值，放到入库前的确认卡上让人改
    result = await chat(message="帮我出几道题")
    assert result.ask is not None
    keys = {f.key for f in result.ask.fields}
    assert keys == {"count", "topic"}


async def test_unique_bank_is_auto_selected(monkeypatch):
    """唯一匹配直接选中，不该问用户"""
    monkeypatch.setattr(flows, "call_tool", _tool({"list_question_banks": {"items": [FAKE_BANK]}}))
    monkeypatch.setattr(flows, "call_skill", _skill([FAKE_QUESTION]))
    result = await chat(message="帮我创建2道关于计算机基础的题到计算机题库")
    assert result.ask is not None
    assert result.ask.kind == "confirm", "入库前必须确认"
    assert "计算机基础" in result.ask.title
    assert any(s.type == "tool" and s.ref == "list_question_banks" for s in result.trace)


async def test_multiple_banks_require_choice(monkeypatch):
    """多个匹配 -> 让人选"""
    monkeypatch.setattr(
        flows,
        "call_tool",
        _tool({"list_question_banks": {"items": [FAKE_BANK, {"id": "1002", "name": "计算机网络", "questionCount": 3}]}}),
    )
    result = await chat(message="帮我创建2道关于计算机的题到计算机题库")
    assert result.ask is not None
    assert result.ask.kind == "form"
    assert [f.key for f in result.ask.fields] == ["bank_id"]


async def test_no_bank_offers_create_or_pick(monkeypatch):
    """没有匹配 -> 给「新建 / 挑一个」两条路"""
    monkeypatch.setattr(flows, "call_tool", _tool({"list_question_banks": {"items": []}}))
    result = await chat(message="帮我创建2道关于量子力学的题")
    assert result.ask is not None
    assert {f.key for f in result.ask.fields} == {"bank_mode", "new_bank_name"}


async def test_confirm_then_write(monkeypatch):
    """确认后才真正写库，并给出入库结果"""
    monkeypatch.setattr(
        flows,
        "call_tool",
        _tool({
            "list_question_banks": {"items": [FAKE_BANK]},
            "save_questions": {"ids": ["9001", "9002"], "count": 2, "bankId": "1001"},
        }),
    )
    monkeypatch.setattr(flows, "call_skill", _skill([FAKE_QUESTION, FAKE_QUESTION]))

    first = await chat(message="帮我创建2道关于计算机基础的题到计算机题库")
    assert first.ask is not None and first.ask.kind == "confirm"

    second = await chat(message="", session_id=first.session_id, answers={"confirmed": True})
    assert second.ask is None
    assert "已完成" in second.reply
    assert "计算机基础" in second.reply
    assert any(s.type == "tool" and s.ref == "save_questions" for s in second.trace)


async def test_tool_failure_degrades_to_manual_input(monkeypatch):
    """题库服务挂了不能整轮崩掉，降级成让用户手填题库 ID"""
    monkeypatch.setattr(flows, "call_tool", _tool({}))
    result = await chat(message="帮我创建2道关于计算机基础的题")
    assert result.ask is not None
    assert [f.key for f in result.ask.fields] == ["bank_id"]


# ---------------------------------------------------------------- 答题分析

FAKE_STATS = {
    "examId": "7", "examName": "期中考试", "total": 30, "submitted": 28,
    "avgScore": 72.5, "maxScore": 98, "minScore": 30, "passRate": "72%",
    "scoreBands": [{"band": "0-59", "count": 5}, {"band": "60-79", "count": 12}],
}


async def test_exam_analysis_runs_tools_then_answers(monkeypatch):
    monkeypatch.setattr(
        flows,
        "call_tool",
        _tool({"find_exam": {"items": [{"id": "7", "name": "期中考试"}]}, "exam_answer_stats": FAKE_STATS}),
    )

    async def fake_llm(prompt_code, variables, ctx, title="", step_type="skill") -> tuple[str, TraceStep]:
        return "- 整体中等偏上", TraceStep(type="skill", ref=prompt_code, status="ok")

    monkeypatch.setattr(flows, "call_llm", fake_llm)

    result = await chat(message="期中考试考卷的答题情况怎么样")
    assert result.ask is None
    assert "期中考试" in result.reply
    assert "72.5" in result.reply
    refs = [s.ref for s in result.trace]
    assert "find_exam" in refs and "exam_answer_stats" in refs


async def test_exam_analysis_falls_back_when_llm_empty(monkeypatch):
    """模型没吐内容时用规则化结论兜底，保证对话永远有话说"""
    monkeypatch.setattr(
        flows, "call_tool", _tool({"find_exam": {"items": [{"id": "7", "name": "期中考试"}]}, "exam_answer_stats": FAKE_STATS})
    )

    async def empty_llm(*args, **kwargs) -> tuple[str, TraceStep]:
        return "", TraceStep(type="skill", status="error")

    monkeypatch.setattr(flows, "call_llm", empty_llm)
    result = await chat(message="期中考试的答题情况")
    assert result.ask is None
    assert "交卷率" in result.reply
