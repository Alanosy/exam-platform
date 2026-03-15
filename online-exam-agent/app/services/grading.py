"""主观题批改服务：读取作答 -> Agent 评分 -> 回写 ai_score/ai_reason。"""
import logging

from sqlalchemy.orm import Session

import app.db_models as db_models
from app.agents.grading import grade_questions

logger = logging.getLogger(__name__)


def grade(db: Session, exam_id: int, user_id: int) -> list[dict]:
    """对某次考试中某学生的简答题进行 AI 批改并回写。"""
    items = db_models.get_questions_for_grading(db, exam_id, user_id)
    if not items:
        return []

    results = grade_questions(items)
    out = []
    for r in results:
        score = max(0, r.score or 0)
        reason = f"得分点：{r.scoringPoints or ''} | 错误分析：{r.errorAnalysis or ''}"
        db_models.update_grading_result(
            db,
            exam_id=exam_id, user_id=user_id,
            question_id=r.questionId, ai_score=score, ai_reason=reason,
        )
        out.append({
            "questionId": r.questionId,
            "score": score,
            "scoringPoints": r.scoringPoints,
            "errorAnalysis": r.errorAnalysis,
        })
    db.commit()
    return out
