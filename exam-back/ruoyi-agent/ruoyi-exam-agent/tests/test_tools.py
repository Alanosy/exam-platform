"""工具层与护栏单测

护栏是硬约束的落地：**凡是会影响考生命运的动作，一律不放进自主循环。**
这个测试文件是那条铁律的守门人——谁删了护栏，这里会红。
"""

from __future__ import annotations

import pytest

from app.agent.guardrail import (
    HIGH_RISK_TOOLS,
    check_step,
    check_tool,
    guardrail_policy,
    needs_human_review,
)
from app.core.errors import GuardrailBlockedError
from app.tools.base import READ, WRITE
from app.tools.catalog import TOOLS, get_tool, list_tools, visible_tools


def test_tool_catalog_size():
    # 新增工具时同步改这里：逼着改代码的人回头看一眼清单是否还自洽
    assert len(TOOLS) == 24, "工具数量变了，请同步更新本断言与文档"


def test_student_never_sees_admin_tools():
    """角色隔离：学生的会话里不能出现管理端工具"""
    codes = {t.code for t in visible_tools("student")}
    assert "save_questions" not in codes, "学生不该看到批量入库"
    assert "submit_mark_score" not in codes, "学生不该看到改分"
    assert "publish_exam" not in codes, "学生不该看到发布考试"
    # 通用出口与考生视角工具必须还在
    assert {"api_call", "api_manifest", "whoami", "resolve_entity"} <= codes
    assert "list_wrong_questions" in codes


def test_admin_sees_write_tools():
    codes = {t.code for t in visible_tools("admin")}
    assert "save_questions" in codes
    assert "submit_mark_score" in codes


def test_every_tool_has_path_and_risk():
    for code, spec in TOOLS.items():
        assert spec.path.startswith("/api/exam-tool/"), f"{code} 路径不规范"
        assert spec.risk_level in (READ, WRITE), f"{code} risk_level 非法"
        assert spec.description, f"{code} 缺少描述，Agent 会不知道什么时候用它"


def test_list_tools_shape():
    items = list_tools()
    assert len(items) == len(TOOLS)
    assert {"code", "name", "risk_level", "endpoint"} <= set(items[0])


def test_high_risk_tools_blocked_without_approval():
    for code in HIGH_RISK_TOOLS:
        spec = get_tool(code)
        with pytest.raises(GuardrailBlockedError):
            check_tool(code, spec.risk_level, approved=False)


def test_high_risk_tools_allowed_when_approved():
    for code in HIGH_RISK_TOOLS:
        spec = get_tool(code)
        check_tool(code, spec.risk_level, approved=True)  # 不抛异常即通过


def test_write_tool_needs_approval():
    with pytest.raises(GuardrailBlockedError):
        check_tool("save_question", WRITE, approved=False)


def test_read_tool_is_free():
    check_tool("get_exam", READ, approved=False)


def test_step_limit():
    check_step(1)
    check_step(12)
    with pytest.raises(GuardrailBlockedError):
        check_step(13)


def test_needs_human_review_rules():
    assert needs_human_review({"confidence": 0.3}) is True
    assert needs_human_review({"confidence": 0.9}) is False
    assert needs_human_review({"need_human": True}) is True
    assert needs_human_review({"passed": False}) is True


def test_guardrail_policy_exposed():
    policy = guardrail_policy()
    assert policy["high_risk_tools"] == sorted(HIGH_RISK_TOOLS)
    assert policy["confidence_floor"] == 0.6
    assert "rule" in policy
