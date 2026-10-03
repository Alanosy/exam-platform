"""Skill 的输入输出契约

题型与答案 JSON 契约**严格对齐项目现有的落库格式**
（见 memory：question.answer / exam_answer.answer_content 两套 JSON），
这样 AI 生成的题目可以直接被 Java 侧消费，不需要二次转换。

答案契约：
    SINGLE/MULTIPLE : {"rightKeys": ["A","C"]}
    JUDGE           : {"rightKeys": ["A"]}   A=正确 B=错误
    BLANK           : {"blanks": [{"answers": ["北京","北平"]}]}
    MATCH           : {"pairs": [{"left": "CPU", "right": "中央处理器"}]}
    SHORT_ANSWER/ESSAY/UPLOAD_FILE : {"answer": "..."}
    CODE            : {"language": "java", "answer": "...", "remark": "..."}
"""

from __future__ import annotations

from typing import Any

from pydantic import BaseModel, Field

QUESTION_TYPES = (
    "SINGLE", "MULTIPLE", "JUDGE", "BLANK", "MATCH",
    "SHORT_ANSWER", "ESSAY", "CODE", "UPLOAD_FILE",
)
DIFFICULTIES = ("easy", "medium", "hard")


# ---------------- 题目结构 ----------------

class Option(BaseModel):
    key: str = ""
    content: str = ""


class GeneratedQuestion(BaseModel):
    """AI 生成的题目"""

    question_type: str = ""
    stem: str = ""
    options: list[Option] | None = None
    answer: str | None = Field(default=None, description="字符串形式的 JSON")
    analysis: str | None = None
    knowledge_points: list[str] | None = None
    difficulty: str | None = None
    score: float | None = None


# ---------------- 出题 ----------------

class QuestionGenInput(BaseModel):
    question_type: str = Field(default="SINGLE", description="题型 code")
    difficulty: str = Field(default="medium")
    knowledge_points: list[str] = Field(default_factory=list)
    count: int = Field(default=5, ge=1, le=30)
    score: float = Field(default=5)
    rag_context: str = ""
    extra: dict[str, Any] = Field(default_factory=dict)


class DistractorGenInput(BaseModel):
    stem: str
    correct_key: str = "A"
    correct_content: str = ""
    existing_options: list[Option] | None = None
    count: int = Field(default=3, ge=1, le=4)


class DistractorItem(BaseModel):
    key: str = ""
    content: str = ""
    trap: str = ""


class QuestionRewriteInput(BaseModel):
    question_type: str
    stem: str
    options: list[Option] | None = None
    answer: str | None = None
    strategy: str = Field(
        default="change_scene",
        description="change_scene / change_number / reverse / deeper",
    )
    count: int = Field(default=3, ge=1, le=10)
    extra: dict[str, Any] = Field(default_factory=dict)


class KnowledgeTagInput(BaseModel):
    question_type: str = ""
    stem: str
    options: list[Option] | None = None
    answer: str | None = None
    candidates: list[str] | None = None


class KnowledgeTagOutput(BaseModel):
    knowledge_points: list[str] = Field(default_factory=list)
    difficulty: str = "medium"
    cognitive_level: str = "understand"
    confidence: float = 0.0


class AnalysisGenInput(BaseModel):
    question_type: str = ""
    stem: str
    options: list[Option] | None = None
    answer: str | None = None


class AnalysisGenOutput(BaseModel):
    analysis: str = ""
    key_steps: list[str] = Field(default_factory=list)


# ---------------- 质检 ----------------

class QuestionAuditInput(BaseModel):
    question_type: str = ""
    difficulty: str = ""
    knowledge_points: list[str] | None = None
    stem: str
    options: list[Option] | None = None
    answer: str | None = None
    analysis: str | None = None
    rag_context: str = ""


class AuditIssue(BaseModel):
    level: str = "minor"
    type: str = ""
    detail: str = ""
    suggestion: str = ""


class QuestionAuditOutput(BaseModel):
    passed: bool = False
    quality_score: float = 0.0
    issues: list[AuditIssue] = Field(default_factory=list)
    summary: str = ""


# ---------------- 阅卷 ----------------

class MarkScoreInput(BaseModel):
    question_type: str = "SHORT_ANSWER"
    stem: str
    full_score: float = Field(default=10, gt=0)
    standard_answer: str = ""
    rubric: str = ""
    analysis: str = ""
    answer_text: str
    anchor_high: str = ""
    anchor_low: str = ""


class MatchedPoint(BaseModel):
    point: str = ""
    got: bool = False
    score: float = 0


class MarkScoreOutput(BaseModel):
    score: float = 0
    full_score: float = 0
    confidence: float = 0
    matched_points: list[MatchedPoint] = Field(default_factory=list)
    reason: str = ""
    comment: str = ""
    need_human: bool = False


class MarkReflectInput(BaseModel):
    question_type: str = ""
    stem: str
    full_score: float = 10
    standard_answer: str = ""
    answer_text: str
    ai_score: float
    ai_reason: str = ""
    matched_points: list[MatchedPoint] | None = None


class MarkReflectOutput(BaseModel):
    accepted: bool = True
    adjusted_score: float = 0
    score_quality: float = 0
    issues: list[str] = Field(default_factory=list)
    comment: str = ""


class CodeJudgeInput(BaseModel):
    stem: str
    language: str = "java"
    full_score: float = 10
    standard_answer: str = ""
    answer_text: str
    test_result: str = ""


class ScoringCalibInput(BaseModel):
    stem: str = ""
    full_score: float = 10
    samples: list[dict[str, Any]] = Field(
        default_factory=list, description="[{ai_score, human_score}]"
    )


class SimilarityDetectInput(BaseModel):
    stem: str = ""
    answers: list[dict[str, Any]] = Field(
        default_factory=list, description="[{id, content}]"
    )


# ---------------- 学情 ----------------

class DiagnosisInput(BaseModel):
    wrong_items: list[dict[str, Any]] = Field(default_factory=list)
    mastered: dict[str, Any] | None = None


class RecommendInput(BaseModel):
    profile: dict[str, Any] | None = None
    weak_points: list[str] | None = None
    candidates: list[dict[str, Any]] = Field(default_factory=list)
    target_count: int = Field(default=5, ge=1, le=10)


class RecommendItem(BaseModel):
    question_id: str = ""
    reason: str = ""
    priority: int = 5
    target_point: str = ""


# ---------------- 试卷 ----------------

class PaperReviewInput(BaseModel):
    title: str = ""
    duration: int = Field(default=60, description="考试时长（分钟）")
    pass_score: float = 60
    questions: list[dict[str, Any]] = Field(default_factory=list)
    focus: str = "全面审查"


class PaperDifficultyInput(BaseModel):
    title: str = ""
    duration: int = 60
    total_score: float = 100
    pass_score: float = 60
    questions: list[dict[str, Any]] = Field(default_factory=list)
    audience: str = "全体考生"
    history: str = ""


# ---------------- 监考 / 报告 ----------------

class ProctorAnalyzeInput(BaseModel):
    user_name: str = ""
    exam_name: str = ""
    duration: int = 60
    events: list[dict[str, Any]] = Field(default_factory=list)
    thresholds: dict[str, Any] | None = None


class ReportWriteInput(BaseModel):
    subject: str
    audience: str = "管理者"
    stats: Any = None
