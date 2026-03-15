"""智能出题相关 Pydantic 模型。"""
from typing import List, Optional

from pydantic import BaseModel


class QuestionGenRequest(BaseModel):
    """出题请求：知识点、难度、题型、题量、题库"""
    repoId: int
    knowledgePoints: str = ""
    difficulty: str = "中等"
    quType: int          # 1单选 2多选 3判断 4简答
    count: int = 3


class GeneratedOption(BaseModel):
    isRight: int = 0
    content: str = ""
    sort: Optional[int] = None


class GeneratedQuestion(BaseModel):
    """生成的题目（预览/入库）"""
    quType: int
    content: str
    analysis: Optional[str] = None
    image: Optional[str] = None
    answer: Optional[str] = None          # 简答题标准答案
    options: Optional[List[GeneratedOption]] = None
    duplicated: Optional[bool] = False    # 是否与题库重复


class QuestionGenSaveRequest(BaseModel):
    """确认入库请求"""
    request: QuestionGenRequest
    questions: List[GeneratedQuestion]
