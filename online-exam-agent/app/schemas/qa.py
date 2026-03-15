"""考试答疑相关 Pydantic 模型。"""
from pydantic import BaseModel


class QaRequest(BaseModel):
    message: str
