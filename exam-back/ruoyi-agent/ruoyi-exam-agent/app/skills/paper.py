"""试卷域 Skill：审查 + 难度预估"""

from __future__ import annotations

from typing import Any, ClassVar

from app.llm.structured import parse_json
from app.skills.base import READ, AiSkill, SkillContext


def _fmt_questions(items: list[dict[str, Any]] | None) -> str:
    if not items:
        return "（无题目清单）"
    lines = []
    for i, q in enumerate(items):
        lines.append(
            f"{i + 1}. [{q.get('question_type') or '?'}] "
            f"{q.get('difficulty') or '?'} | {q.get('score') or '?'}分 | "
            f"知识点={q.get('knowledge_point') or '未标注'} | "
            f"题干={(q.get('stem') or '')[:80]}"
        )
    return "\n".join(lines)


class PaperReviewSkill(AiSkill):
    """试卷审查"""

    code: ClassVar[str] = "paper_review"
    name: ClassVar[str] = "试卷审查"
    description: ClassVar[str] = "审查知识点覆盖、难度分布、重复度与预计用时"
    prompt_code: ClassVar[str] = "paper_review"
    risk_level: ClassVar[str] = READ

    @property
    def input_model(self) -> Any:
        from app.skills.schemas import PaperReviewInput

        return PaperReviewInput

    def build_vars(self, inp: Any, ctx: SkillContext) -> dict[str, Any]:
        return {
            "title": inp.title or "（未命名试卷）",
            "duration": inp.duration,
            "pass_score": inp.pass_score,
            "questions": _fmt_questions(inp.questions),
            "focus": inp.focus or "全面审查",
        }

    def parse(self, content: str) -> dict[str, Any]:
        return parse_json(content, expect=dict)


class PaperDifficultySkill(AiSkill):
    """试卷难度预估"""

    code: ClassVar[str] = "paper_difficulty"
    name: ClassVar[str] = "试卷难度预估"
    description: ClassVar[str] = "考前预估平均分、通过率与分数分布"
    prompt_code: ClassVar[str] = "paper_difficulty"
    risk_level: ClassVar[str] = READ

    @property
    def input_model(self) -> Any:
        from app.skills.schemas import PaperDifficultyInput

        return PaperDifficultyInput

    def build_vars(self, inp: Any, ctx: SkillContext) -> dict[str, Any]:
        return {
            "title": inp.title or "（未命名试卷）",
            "duration": inp.duration,
            "total_score": inp.total_score,
            "pass_score": inp.pass_score,
            "questions": _fmt_questions(inp.questions),
            "audience": inp.audience or "全体考生",
            "history": inp.history or "（无历史数据）",
        }

    def parse(self, content: str) -> dict[str, Any]:
        return parse_json(content, expect=dict)
