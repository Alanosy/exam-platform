"""阅卷域 Skill

最重要的约定：**AI 只给建议分，不给定分。**
MarkScoreSkill 输出的 need_human=True 时，Java 侧必须转人工，不能自动落分。
"""

from __future__ import annotations

from typing import Any, ClassVar

from app.llm.structured import parse_model
from app.skills.base import READ, AiSkill, SkillContext, as_text
from app.skills.schemas import (
    CodeJudgeInput,
    MarkReflectInput,
    MarkReflectOutput,
    MarkScoreInput,
    MarkScoreOutput,
    ScoringCalibInput,
    SimilarityDetectInput,
)

# 置信度低于此值时，判定为需要人工复核
CONFIDENCE_FLOOR = 0.6


class MarkScoreSkill(AiSkill):
    """主观题 AI 评分（建议分）"""

    code: ClassVar[str] = "mark_score"
    name: ClassVar[str] = "主观题 AI 评分"
    description: ClassVar[str] = "对简答/论述/填空给出建议分、要点命中与置信度"
    prompt_code: ClassVar[str] = "mark_score"
    risk_level: ClassVar[str] = READ
    require_human_review: ClassVar[bool] = True  # 分数永远需要人确认

    @property
    def input_model(self) -> type[MarkScoreInput]:
        return MarkScoreInput

    def build_vars(self, inp: MarkScoreInput, ctx: SkillContext) -> dict[str, Any]:
        return {
            "question_type": inp.question_type,
            "stem": inp.stem,
            "full_score": inp.full_score,
            "standard_answer": inp.standard_answer or "（无参考答案，请按题干合理判断）",
            "rubric": inp.rubric or "（无评分要点，请按参考答案自行拆分）",
            "analysis": inp.analysis or "（无）",
            "answer_text": inp.answer_text,
            "anchor_high": inp.anchor_high or "（无）",
            "anchor_low": inp.anchor_low or "（无）",
        }

    def parse(self, content: str) -> MarkScoreOutput:
        out = parse_model(content, MarkScoreOutput)
        # 兜底约束：分数必须落在 [0, full_score]，不能被模型写飞
        if out.full_score <= 0:
            out.full_score = 10
        out.score = max(0.0, min(float(out.score), float(out.full_score)))
        out.confidence = max(0.0, min(float(out.confidence), 1.0))
        # 置信度不足一律转人工——宁可不给，不要乱给
        if out.confidence < CONFIDENCE_FLOOR:
            out.need_human = True
        return out


class MarkReflectSkill(AiSkill):
    """评分自检（Reflection）"""

    code: ClassVar[str] = "mark_reflect"
    name: ClassVar[str] = "评分自检"
    description: ClassVar[str] = "对已给出的 AI 评分做二次校验，发现宽严失当"
    prompt_code: ClassVar[str] = "mark_reflect"
    risk_level: ClassVar[str] = READ

    @property
    def input_model(self) -> type[MarkReflectInput]:
        return MarkReflectInput

    def build_vars(self, inp: MarkReflectInput, ctx: SkillContext) -> dict[str, Any]:
        return {
            "question_type": inp.question_type or "（未标注）",
            "stem": inp.stem,
            "full_score": inp.full_score,
            "standard_answer": inp.standard_answer or "（无）",
            "answer_text": inp.answer_text,
            "ai_score": inp.ai_score,
            "ai_reason": inp.ai_reason or "（无）",
            "matched_points": as_text(
                [p.model_dump() for p in (inp.matched_points or [])]
            ),
        }

    def parse(self, content: str) -> MarkReflectOutput:
        out = parse_model(content, MarkReflectOutput)
        out.adjusted_score = max(0.0, float(out.adjusted_score))
        return out


class CodeJudgeSkill(AiSkill):
    """代码题判分"""

    code: ClassVar[str] = "code_judge"
    name: ClassVar[str] = "代码题判分"
    description: ClassVar[str] = "结合运行结果与代码质量对编程题评分"
    prompt_code: ClassVar[str] = "code_judge"
    risk_level: ClassVar[str] = READ
    require_human_review: ClassVar[bool] = True

    @property
    def input_model(self) -> type[CodeJudgeInput]:
        return CodeJudgeInput

    def build_vars(self, inp: CodeJudgeInput, ctx: SkillContext) -> dict[str, Any]:
        return {
            "stem": inp.stem,
            "language": inp.language,
            "full_score": inp.full_score,
            "standard_answer": inp.standard_answer or "（无）",
            "answer_text": inp.answer_text,
            "test_result": inp.test_result or "（未执行测试，请静态分析）",
        }

    def parse(self, content: str) -> dict[str, Any]:
        from app.llm.structured import parse_json

        data = parse_json(content, expect=dict)
        full = float(data.get("full_score") or 10)
        data["score"] = max(0.0, min(float(data.get("score") or 0), full))
        return data


class ScoringCalibSkill(AiSkill):
    """评分校准：AI 分 vs 教师分"""

    code: ClassVar[str] = "scoring_calib"
    name: ClassVar[str] = "评分校准"
    description: ClassVar[str] = "对比 AI 分与教师历史分，给出宽严偏差与校准系数"
    prompt_code: ClassVar[str] = "scoring_calib"
    risk_level: ClassVar[str] = READ

    @property
    def input_model(self) -> type[ScoringCalibInput]:
        return ScoringCalibInput

    def build_vars(self, inp: ScoringCalibInput, ctx: SkillContext) -> dict[str, Any]:
        lines = [
            f"{i + 1}. AI {s.get('ai_score')} / 教师 {s.get('human_score')}"
            for i, s in enumerate(inp.samples)
        ]
        return {
            "stem": inp.stem or "（未指定）",
            "full_score": inp.full_score,
            "samples": "\n".join(lines) or "（无样本）",
        }

    def parse(self, content: str) -> dict[str, Any]:
        from app.llm.structured import parse_json

        return parse_json(content, expect=dict)


class SimilarityDetectSkill(AiSkill):
    """作答雷同检测"""

    code: ClassVar[str] = "similarity_detect"
    name: ClassVar[str] = "作答雷同检测"
    description: ClassVar[str] = "对多份主观题作答做相似度聚类，辅助判定抄袭"
    prompt_code: ClassVar[str] = "similarity_detect"
    risk_level: ClassVar[str] = READ
    require_human_review: ClassVar[bool] = True  # 抄袭认定必须人工

    @property
    def input_model(self) -> type[SimilarityDetectInput]:
        return SimilarityDetectInput

    def build_vars(self, inp: SimilarityDetectInput, ctx: SkillContext) -> dict[str, Any]:
        lines = [f"[{a.get('id')}] {a.get('content')}" for a in inp.answers]
        return {
            "stem": inp.stem or "（未指定）",
            "answers": "\n".join(lines) or "（无）",
        }

    def parse(self, content: str) -> dict[str, Any]:
        from app.llm.structured import parse_json

        return parse_json(content, expect=dict)
