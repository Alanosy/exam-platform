"""主观题批改相关 Pydantic 模型。"""
from typing import List, Optional

from pydantic import BaseModel


class GradingRequest(BaseModel):
    """批改请求：考试ID + 学生ID"""
    examId: int
    userId: int


class GradingResult(BaseModel):
    """单题批改结果"""
    questionId: int
    score: int = 0
    scoringPoints: Optional[str] = None
    errorAnalysis: Optional[str] = None
