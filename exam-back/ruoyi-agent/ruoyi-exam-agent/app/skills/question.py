"""命题域 Skill：出题 / 干扰项 / 改写 / 打标 / 解析"""

from __future__ import annotations

from typing import Any, ClassVar

from app.llm.structured import parse_model, parse_model_list
from app.skills.base import READ, AiSkill, SkillContext, as_text
from app.skills.schemas import (
    AnalysisGenInput,
    AnalysisGenOutput,
    DistractorGenInput,
    DistractorItem,
    GeneratedQuestion,
    KnowledgeTagInput,
    KnowledgeTagOutput,
    QuestionGenInput,
    QuestionRewriteInput,
)


class QuestionGenSkill(AiSkill):
    """按知识点/难度/题型生成试题"""

    code: ClassVar[str] = "question_gen"
    name: ClassVar[str] = "智能出题"
    description: ClassVar[str] = "按知识点、难度、题型批量生成试题，可结合参考资料"
    prompt_code: ClassVar[str] = "question_gen"
    risk_level: ClassVar[str] = READ

    @property
    def input_model(self) -> type[QuestionGenInput]:
        return QuestionGenInput

    def build_vars(self, inp: QuestionGenInput, ctx: SkillContext) -> dict[str, Any]:
        return {
            "question_type": inp.question_type,
            "difficulty": inp.difficulty,
            "knowledge_points": "、".join(inp.knowledge_points) or "（未指定，请自行命题）",
            "count": inp.count,
            "score": inp.score,
            "rag_context": inp.rag_context or "（无参考资料）",
            "extra": as_text(inp.extra, "（无）"),
        }

    def parse(self, content: str) -> list[GeneratedQuestion]:
        return parse_model_list(content, GeneratedQuestion)


class DistractorGenSkill(AiSkill):
    """生成干扰项"""

    code: ClassVar[str] = "distractor_gen"
    name: ClassVar[str] = "干扰项生成"
    description: ClassVar[str] = "为已有正确选项的题干生成有迷惑性的干扰项"
    prompt_code: ClassVar[str] = "distractor_gen"
    risk_level: ClassVar[str] = READ

    @property
    def input_model(self) -> type[DistractorGenInput]:
        return DistractorGenInput

    def build_vars(self, inp: DistractorGenInput, ctx: SkillContext) -> dict[str, Any]:
        existing = inp.existing_options or []
        return {
            "stem": inp.stem,
            "correct_key": inp.correct_key,
            "correct_content": inp.correct_content,
            "existing_options": as_text(
                [f"{o.key}. {o.content}" for o in existing] if existing else None
            ),
            "count": inp.count,
        }

    def parse(self, content: str) -> list[DistractorItem]:
        return parse_model_list(content, DistractorItem)


class QuestionRewriteSkill(AiSkill):
    """试题改写扩量"""

    code: ClassVar[str] = "question_rewrite"
    name: ClassVar[str] = "试题改写扩量"
    description: ClassVar[str] = "对已有试题做等价变体改写，用于题库扩量与防泄露"
    prompt_code: ClassVar[str] = "question_rewrite"
    risk_level: ClassVar[str] = READ

    @property
    def input_model(self) -> type[QuestionRewriteInput]:
        return QuestionRewriteInput

    def build_vars(self, inp: QuestionRewriteInput, ctx: SkillContext) -> dict[str, Any]:
        return {
            "question_type": inp.question_type,
            "stem": inp.stem,
            "options": as_text([f"{o.key}. {o.content}" for o in (inp.options or [])]),
            "answer": inp.answer or "（无）",
            "strategy": inp.strategy,
            "count": inp.count,
            "extra": as_text(inp.extra),
        }

    def parse(self, content: str) -> list[GeneratedQuestion]:
        return parse_model_list(content, GeneratedQuestion)


class KnowledgeTagSkill(AiSkill):
    """知识点打标（补存量题的字段缺口）"""

    code: ClassVar[str] = "knowledge_tag"
    name: ClassVar[str] = "知识点打标"
    description: ClassVar[str] = "为存量试题补充知识点、难度与认知层次标注"
    prompt_code: ClassVar[str] = "knowledge_tag"
    risk_level: ClassVar[str] = READ

    @property
    def input_model(self) -> type[KnowledgeTagInput]:
        return KnowledgeTagInput

    def build_vars(self, inp: KnowledgeTagInput, ctx: SkillContext) -> dict[str, Any]:
        return {
            "question_type": inp.question_type or "（未标注）",
            "stem": inp.stem,
            "options": as_text([f"{o.key}. {o.content}" for o in (inp.options or [])]),
            "answer": inp.answer or "（无）",
            "candidates": "、".join(inp.candidates) if inp.candidates else "（无，可自行归纳）",
        }

    def parse(self, content: str) -> KnowledgeTagOutput:
        return parse_model(content, KnowledgeTagOutput)


class AnalysisGenSkill(AiSkill):
    """批量补解析"""

    code: ClassVar[str] = "analysis_gen"
    name: ClassVar[str] = "试题解析生成"
    description: ClassVar[str] = "为已有答案的试题批量生成解析"
    prompt_code: ClassVar[str] = "analysis_gen"
    risk_level: ClassVar[str] = READ

    @property
    def input_model(self) -> type[AnalysisGenInput]:
        return AnalysisGenInput

    def build_vars(self, inp: AnalysisGenInput, ctx: SkillContext) -> dict[str, Any]:
        return {
            "question_type": inp.question_type or "（未标注）",
            "stem": inp.stem,
            "options": as_text([f"{o.key}. {o.content}" for o in (inp.options or [])]),
            "answer": inp.answer or "（无）",
        }

    def parse(self, content: str) -> AnalysisGenOutput:
        return parse_model(content, AnalysisGenOutput)
