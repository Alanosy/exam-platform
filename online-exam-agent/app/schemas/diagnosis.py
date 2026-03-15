"""学情诊断相关 Pydantic 模型。"""
from typing import List, Optional

from pydantic import BaseModel


class WeakPoint(BaseModel):
    knowledgePoint: str
    wrongCount: int = 0
    analysis: str = ""


class RecommendType(BaseModel):
    quType: int           # 1单选 2多选 3判断 4简答
    quTypeName: str = ""
    reason: str = ""


class DiagnosisResult(BaseModel):
    """学情诊断结果"""
    weakPoints: Optional[List[WeakPoint]] = None
    reviewSuggestions: Optional[List[str]] = None
    recommendTypes: Optional[List[RecommendType]] = None
