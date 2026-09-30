"""主观题批改 Agent 接口。"""
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_db
from app.schemas.common import Result
from app.schemas.grading import GradingRequest
from app.services import grading

router = APIRouter()


@router.post("/subjective", response_model=Result)
def grade(req: GradingRequest, db: Session = Depends(get_db)):
    """主观题 AI 批改（交卷后），回写 ai_score/ai_reason。"""
    data = grading.grade(db, req.examId, req.userId)
    return Result.success(data)
