"""监考与报告 Skill

注意：涉及作弊认定的输出一律 require_human_review=True。
AI 只整理证据链，**不认定作弊**。
"""

from __future__ import annotations

import json
from typing import Any, ClassVar

from app.llm.structured import parse_json
from app.skills.base import READ, AiSkill, SkillContext


class ProctorAnalyzeSkill(AiSkill):
    """监考行为分析"""

    code: ClassVar[str] = "proctor_analyze"
    name: ClassVar[str] = "监考行为分析"
    description: ClassVar[str] = "分析监考事件时序，输出可疑程度与证据链（不代替人工判定）"
    prompt_code: ClassVar[str] = "proctor_analyze"
    risk_level: ClassVar[str] = READ
    require_human_review: ClassVar[bool] = True  # 作弊认定必须人工

    @property
    def input_model(self) -> Any:
        from app.skills.schemas import ProctorAnalyzeInput

        return ProctorAnalyzeInput

    def build_vars(self, inp: Any, ctx: SkillContext) -> dict[str, Any]:
        lines = []
        for e in inp.events or []:
            lines.append(
                f"{e.get('event_time') or '?'} | {e.get('event_name') or e.get('event_type')} | "
                f"level={e.get('level') or 'info'} | {e.get('content') or ''}"
            )
        return {
            "user_name": inp.user_name or "（未知）",
            "exam_name": inp.exam_name or "（未知）",
            "duration": inp.duration,
            "events": "\n".join(lines) or "（无事件记录）",
            "thresholds": json.dumps(inp.thresholds, ensure_ascii=False) if inp.thresholds else "（未配置阈值）",
        }

    def parse(self, content: str) -> dict[str, Any]:
        data = parse_json(content, expect=dict)
        data["need_human"] = True  # 强制：不允许模型自己说不需要人工
        return data


class ReportWriteSkill(AiSkill):
    """分析报告撰写"""

    code: ClassVar[str] = "report_write"
    name: ClassVar[str] = "分析报告撰写"
    description: ClassVar[str] = "把统计数据转成自然语言分析报告"
    prompt_code: ClassVar[str] = "report_write"
    risk_level: ClassVar[str] = READ

    @property
    def input_model(self) -> Any:
        from app.skills.schemas import ReportWriteInput

        return ReportWriteInput

    def build_vars(self, inp: Any, ctx: SkillContext) -> dict[str, Any]:
        stats = inp.stats
        if not isinstance(stats, str):
            stats = json.dumps(stats, ensure_ascii=False, indent=1)
        return {
            "subject": inp.subject,
            "audience": inp.audience or "管理者",
            "stats": stats or "（无数据）",
        }

    def parse(self, content: str) -> dict[str, Any]:
        return parse_json(content, expect=dict)
