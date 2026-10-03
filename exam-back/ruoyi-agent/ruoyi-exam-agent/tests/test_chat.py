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

import json
from typing import Any

import pytest

from app.chat import flows, planner
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


async def test_tool_failure_asks_bank_name_not_id(monkeypatch):
    """题库服务挂了不能整轮崩掉，且只能问名称 —— ID 用户不可能知道"""
    monkeypatch.setattr(flows, "call_tool", _tool({}))
    result = await chat(message="帮我创建2道关于计算机基础的题")
    assert result.ask is not None
    assert [f.key for f in result.ask.fields] == ["bank_name"]
    assert result.ask.fields[0].label == "题库名称"


async def test_manual_bank_name_is_used_as_keyword(monkeypatch):
    """手填的题库名要当成下一轮查询的关键词，而不是丢掉"""
    calls: list[dict] = []

    async def _spy(code, payload, tenant_id="000000", title=""):
        from app.chat.models import TraceStep

        calls.append({"code": code, "payload": payload})
        if code == "list_question_banks":
            # 第一次（自动匹配）模拟服务不通，第二次（拿手填名字查）才返回数据
            if sum(1 for c in calls if c["code"] == "list_question_banks") > 1:
                return {"items": [FAKE_BANK]}, TraceStep(type="tool", ref=code)
            return None, TraceStep(type="tool", ref=code, status="error")
        return {"ids": ["9001"], "count": 1, "bankId": "1001"}, TraceStep(type="tool", ref=code)

    monkeypatch.setattr(flows, "call_tool", _spy)
    monkeypatch.setattr(flows, "call_skill", _skill([FAKE_QUESTION]))

    first = await chat(message="帮我创建2道关于计算机基础的题")
    assert first.ask is not None and [f.key for f in first.ask.fields] == ["bank_name"]

    second = await chat(message="", session_id=first.session_id, answers={"bank_name": "我自己的题库"})
    assert second.ask is not None and second.ask.kind == "confirm"
    assert any(
        c["code"] == "list_question_banks" and c["payload"].get("keyword") == "我自己的题库"
        for c in calls
    ), f"手填名称没有被当作查询关键词: {calls}"


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


# ---------------------------------------------------------------- 规划执行

def _fake_plan(monkeypatch, plan):
    async def _make(question, ctx, trace, history=""):
        return plan

    monkeypatch.setattr(flows, "make_plan", _make)


async def test_planner_explains_when_infeasible(monkeypatch):
    """做不了必须说清楚为什么，而不是回一句听不懂"""
    from app.chat.planner import Plan

    _fake_plan(monkeypatch, Plan(goal="导出全部用户", feasible=False, reason="缺少 system:user:export 权限"))
    result = await chat(message="把系统里所有用户导出来给我")
    assert result.ask is None
    assert "做不了" in result.reply
    assert "权限" in result.reply


async def test_planner_runs_api_step_with_user_token(monkeypatch):
    """规划出的接口步骤要真跑，并且带上用户身份"""
    from app.chat.models import TraceStep
    from app.chat.planner import Plan, PlanStep

    seen: list[tuple[str, dict]] = []

    async def _tool(code, payload, tenant_id="000000", title=""):
        seen.append((code, payload))
        if code == "api_manifest":
            return {"items": []}, TraceStep(type="tool", ref=code)
        return {"ok": True, "status": 200, "data": {"rows": []}}, TraceStep(type="tool", ref=code)

    async def _llm(prompt_code, variables, ctx, title="", step_type="skill"):
        return "汇总：查到 0 场考试", TraceStep(type="skill", ref=prompt_code)

    # 步骤执行发生在 planner 里，汇总发生在 flows 里，两边各打一份桩
    monkeypatch.setattr(planner, "call_tool", _tool)
    monkeypatch.setattr(flows, "call_llm", _llm)
    _fake_plan(monkeypatch, Plan(
        goal="查有哪些考试",
        steps=[PlanStep(kind="api", ref="GET /exam/list", title="查考试列表", params={"query": {"examName": "期中"}})],
    ))

    result = await chat(message="有哪些考试")
    assert "汇总" in result.reply
    api_calls = [c for c in seen if c[0] == "api_call"]
    assert api_calls, f"没有触发接口调用: {seen}"
    assert api_calls[0][1]["path"] == "/exam/list"
    assert api_calls[0][1]["query"] == {"examName": "期中"}
    # 身份必须随调用一起发出去，否则网关认不出是谁
    assert "token" in api_calls[0][1] and "clientId" in api_calls[0][1]


async def test_write_plan_needs_confirmation(monkeypatch):
    """含写操作的计划必须先弹确认卡"""
    from app.chat.planner import Plan, PlanStep

    _fake_plan(monkeypatch, Plan(
        goal="给考试改分",
        steps=[PlanStep(kind="api", ref="POST /mark/score", title="提交评分", risk="write")],
    ))
    result = await chat(message="把张三的分数改成 90")
    assert result.ask is not None
    assert result.ask.kind == "confirm"


# ---------------------------------------------------------------- 角色隔离


def test_identity_audience():
    from app.chat.planner import identity_audience

    assert identity_audience({"isSuperAdmin": True}) == "admin"
    assert identity_audience({"examPermissions": ["system:exam:list"]}) == "admin"
    assert identity_audience({"roles": ["student"]}) == "student"
    assert identity_audience({"roles": ["common"], "examPermissions": []}) == "any"
    assert identity_audience(None) == "any"


def test_plan_step_out_of_audience_is_rejected():
    from app.chat.planner import PlanStep, check_step_allowed

    write = PlanStep(kind="tool", ref="submit_mark_score", title="改分", risk="write")
    assert check_step_allowed(write, "student"), "学生的改分步骤必须被拦下"
    assert "管理端" in check_step_allowed(write, "student")
    assert check_step_allowed(write, "admin") == ""
    assert check_step_allowed(write, "any") == ""


async def test_student_is_routed_away_from_admin_flows(monkeypatch):
    """考生问「全班答题情况」不能被管理端流程直接接走"""
    from app.chat.planner import Plan, PlanStep

    _fake_plan(monkeypatch, Plan(
        goal="看我自己的考试",
        steps=[PlanStep(kind="api", ref="GET /answer/record/center", title="查我的考试")],
    ))
    result = await chat(
        message="期中考试答题情况怎么样",
        identity={"roles": ["student"], "examPermissions": []},
        user_id="9",
    )
    # 走的是考生本人接口，而不是管理端的「考试答卷统计」
    assert "/answer/record/center" in json.dumps(result.data, ensure_ascii=False)
    assert "exam_answer_stats" not in json.dumps(result.data, ensure_ascii=False)


# ---------------------------------------------------------------- 规则兜底规划


def test_heuristic_plan_for_exam_analysis():
    from app.chat.heuristic import heuristic_plan

    plan = heuristic_plan("期中考试的答题情况怎么样", audience="admin")
    assert plan is not None
    assert plan.steps[0].ref == "resolve_entity"
    assert plan.steps[0].params["keyword"]
    # 第二步的 ID 必须来自第一步的结果，不能是编的
    assert "{1.items.0.id}" in plan.steps[1].ref


def test_heuristic_plan_for_student():
    from app.chat.heuristic import heuristic_plan

    plan = heuristic_plan("我的错题有哪些", audience="student")
    assert plan is not None
    assert plan.steps[0].ref == "GET /practice/wrong/overview"
    # 学生不该被规则规划到管理端接口
    assert heuristic_plan("还有多少没阅", audience="student") is None


def test_heuristic_returns_none_when_unknown():
    from app.chat.heuristic import heuristic_plan

    assert heuristic_plan("今天天气怎么样", audience="admin") is None


def test_resolve_refs_replaces_previous_result():
    from app.chat.planner import resolve_refs

    results = [{"data": {"items": [{"id": "8899", "name": "期中"}]}}]
    assert resolve_refs("GET /exam/{1.items.0.id}/overview", results) == "GET /exam/8899/overview"
    assert resolve_refs({"examId": "{1.items.0.id}"}, results) == {"examId": "8899"}
    # 取不到就原样保留，宁可让下游报错也不要塞个假值进去
    assert resolve_refs("{9.items.0.id}", results) == "{9.items.0.id}"
