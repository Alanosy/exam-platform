"""Skill 注册表

新增 Skill 只需两步：
1. 写 prompts/<code>.yaml
2. 在下面的 _SKILLS 里加一行
"""

from __future__ import annotations

import logging
from typing import Any

from app.skills.base import AiSkill
from app.skills.schemas import *  # noqa: F401,F403  供 /skills/{code}/schema 反射使用

logger = logging.getLogger(__name__)


def _build() -> list[AiSkill]:
    from app.skills.audit import QuestionAuditSkill
    from app.skills.learn import DiagnosisSkill, RecommendSkill
    from app.skills.mark import (
        CodeJudgeSkill,
        MarkReflectSkill,
        MarkScoreSkill,
        ScoringCalibSkill,
        SimilarityDetectSkill,
    )
    from app.skills.paper import PaperDifficultySkill, PaperReviewSkill
    from app.skills.proctor import ProctorAnalyzeSkill, ReportWriteSkill
    from app.skills.question import (
        AnalysisGenSkill,
        DistractorGenSkill,
        KnowledgeTagSkill,
        QuestionGenSkill,
        QuestionRewriteSkill,
    )

    return [
        # ---- 命题域 ----
        QuestionGenSkill(),
        DistractorGenSkill(),
        QuestionRewriteSkill(),
        KnowledgeTagSkill(),
        AnalysisGenSkill(),
        QuestionAuditSkill(),
        # ---- 阅卷域 ----
        MarkScoreSkill(),
        MarkReflectSkill(),
        CodeJudgeSkill(),
        ScoringCalibSkill(),
        SimilarityDetectSkill(),
        # ---- 学情域 ----
        DiagnosisSkill(),
        RecommendSkill(),
        # ---- 试卷域 ----
        PaperReviewSkill(),
        PaperDifficultySkill(),
        # ---- 监考 / 报告 ----
        ProctorAnalyzeSkill(),
        ReportWriteSkill(),
    ]


class SkillRegistry:
    def __init__(self) -> None:
        self._skills: dict[str, AiSkill] = {}

    def load(self) -> int:
        self._skills.clear()
        for s in _build():
            if not s.code:
                logger.warning("Skill 缺少 code，跳过: %s", type(s).__name__)
                continue
            self._skills[s.code] = s
        logger.info("已注册 %d 个 Skill: %s", len(self._skills), ", ".join(self._skills))
        return len(self._skills)

    def get(self, code: str) -> AiSkill | None:
        return self._skills.get(code)

    def list_all(self) -> list[dict[str, Any]]:
        return [s.info() for s in self._skills.values()]

    @property
    def codes(self) -> list[str]:
        return list(self._skills)

    @property
    def size(self) -> int:
        return len(self._skills)


registry = SkillRegistry()
