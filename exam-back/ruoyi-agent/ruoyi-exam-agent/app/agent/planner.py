"""Planner：Plan-and-Execute 的规划器

一期用规则规划（可控、零成本、可预期），
LLM 规划器留了接口但默认关闭——长流程用 LLM 规划容易跑偏，
而且规划和执行的双重 token 消耗在批量阅卷场景下不划算。

每条计划 = 一串步骤，每步要么调 Skill，要么调 Tool。
"""

from __future__ import annotations

import logging
from typing import Any

from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)


class Step(BaseModel):
    """计划中的一步"""

    id: str
    name: str
    kind: str = Field(default="skill", description="skill / tool")
    ref: str = Field(default="", description="Skill 或 Tool 的 code")
    payload_from: str = Field(default="", description="从哪个上游结果取入参")
    description: str = ""


class Plan(BaseModel):
    task_type: str
    name: str
    steps: list[Step] = Field(default_factory=list)
    note: str = ""


# 预置计划模板
PLANS: dict[str, Plan] = {
    "question_batch": Plan(
        task_type="question_batch",
        name="批量出题（生成 + 质检）",
        note="Plan-and-Execute + Reflection：先出题，再逐题质检，未通过的转入人工",
        steps=[
            Step(id="gen", name="生成试题", kind="skill", ref="question_gen", description="按约束批量出题"),
            Step(id="audit", name="试题质检", kind="skill", ref="question_audit", description="逐题自检答案唯一性与歧义"),
        ],
    ),
    "mark_batch": Plan(
        task_type="mark_batch",
        name="批量阅卷（预评 + 自检）",
        note="Tool 拉取待阅 -> 逐条 AI 预评 -> Reflection 自检 -> 低置信度转人工",
        steps=[
            Step(id="fetch", name="拉取待阅明细", kind="tool", ref="list_pending_mark"),
            Step(id="score", name="AI 预评", kind="skill", ref="mark_score"),
            Step(id="reflect", name="评分自检", kind="skill", ref="mark_reflect"),
            Step(id="save", name="保存建议分", kind="tool", ref="save_ai_score"),
        ],
    ),
    "paper_compose": Plan(
        task_type="paper_compose",
        name="智能组卷（检索 + 校验）",
        note="ReAct：题量不足时边查边调，最后做覆盖度与难度校验",
        steps=[
            Step(id="search", name="检索候选题", kind="tool", ref="search_questions"),
            Step(id="review", name="组卷校验", kind="skill", ref="paper_review"),
            Step(id="difficulty", name="难度预估", kind="skill", ref="paper_difficulty"),
        ],
    ),
    "diagnose": Plan(
        task_type="diagnose",
        name="学情诊断与推题",
        note="Memory + Tool：读长期画像 -> 拉错题 -> 归因 -> 推题",
        steps=[
            Step(id="wrong", name="拉取错题本", kind="tool", ref="list_wrong_questions"),
            Step(id="diagnosis", name="错题归因", kind="skill", ref="diagnosis"),
            Step(id="recommend", name="个性化推题", kind="skill", ref="recommend"),
        ],
    ),
    "proctor_report": Plan(
        task_type="proctor_report",
        name="监考报告生成",
        note="Tool 取事件 -> AI 出证据链 -> 报告成文；作弊认定必须人工",
        steps=[
            Step(id="events", name="拉取监考事件", kind="tool", ref="list_proctor_events"),
            Step(id="analyze", name="行为分析", kind="skill", ref="proctor_analyze"),
            Step(id="report", name="生成报告", kind="skill", ref="report_write"),
        ],
    ),
}


def get_plan(task_type: str) -> Plan | None:
    return PLANS.get(task_type)


def list_plans() -> list[dict[str, Any]]:
    return [
        {
            "task_type": p.task_type,
            "name": p.name,
            "note": p.note,
            "steps": [{"id": s.id, "name": s.name, "kind": s.kind, "ref": s.ref} for s in p.steps],
        }
        for p in PLANS.values()
    ]
