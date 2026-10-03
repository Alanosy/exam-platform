"""学情域 Skill：错题归因 + 个性化推题（Memory-Augmented 的消费方）"""

from __future__ import annotations

import json
from typing import Any, ClassVar

from app.llm.structured import parse_json, parse_model_list
from app.skills.base import READ, AiSkill, SkillContext, as_text
from app.skills.schemas import DiagnosisInput, RecommendInput, RecommendItem


class DiagnosisSkill(AiSkill):
    """错题归因"""

    code: ClassVar[str] = "diagnosis"
    name: ClassVar[str] = "错题归因"
    description: ClassVar[str] = "从错题中定位薄弱知识点与错误类型"
    prompt_code: ClassVar[str] = "diagnosis"
    risk_level: ClassVar[str] = READ

    @property
    def input_model(self) -> type[DiagnosisInput]:
        return DiagnosisInput

    def build_vars(self, inp: DiagnosisInput, ctx: SkillContext) -> dict[str, Any]:
        lines = []
        for i, it in enumerate(inp.wrong_items or []):
            lines.append(
                f"{i + 1}. 知识点={it.get('knowledge_point') or '未标注'} | "
                f"题型={it.get('question_type') or '?'} | "
                f"我的作答={it.get('my_answer') or '?'} | "
                f"正确答案={it.get('standard_answer') or '?'}"
            )
        return {
            "wrong_items": "\n".join(lines) or "（无错题记录）",
            "mastered": json.dumps(inp.mastered, ensure_ascii=False) if inp.mastered else "（无）",
        }

    def parse(self, content: str) -> dict[str, Any]:
        return parse_json(content, expect=dict)


class RecommendSkill(AiSkill):
    """个性化推题"""

    code: ClassVar[str] = "recommend"
    name: ClassVar[str] = "个性化推题"
    description: ClassVar[str] = "基于学情画像推荐复习题目（遗忘曲线 + 循序渐进）"
    prompt_code: ClassVar[str] = "recommend"
    risk_level: ClassVar[str] = READ

    @property
    def input_model(self) -> type[RecommendInput]:
        return RecommendInput

    def build_vars(self, inp: RecommendInput, ctx: SkillContext) -> dict[str, Any]:
        cand = []
        for c in inp.candidates or []:
            cand.append(
                f"- id={c.get('id')} | {c.get('question_type') or '?'} | "
                f"{c.get('difficulty') or '?'} | {c.get('knowledge_point') or '?'}"
            )
        return {
            "profile": json.dumps(inp.profile, ensure_ascii=False) if inp.profile else "（无）",
            "weak_points": "、".join(inp.weak_points or []) or "（无）",
            "candidates": "\n".join(cand) or "（无候选题，请给出知识点建议）",
            "target_count": inp.target_count,
        }

    def parse(self, content: str) -> list[RecommendItem]:
        return parse_model_list(content, RecommendItem)


__all__ = ["DiagnosisSkill", "RecommendSkill", "as_text"]
