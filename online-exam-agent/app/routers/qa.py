"""考试答疑 Agent 接口（RAG）。"""
from fastapi import APIRouter

from app.schemas.common import Result
from app.schemas.qa import QaRequest
from app.services import exam_qa

router = APIRouter()


@router.post("/chat", response_model=Result)
def chat(req: QaRequest):
    """考试答疑（基于知识库，不泄露原题答案）。"""
    answer = exam_qa.chat(req.message)
    return Result.success(answer)
