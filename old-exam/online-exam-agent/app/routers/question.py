"""智能出题 Agent 接口。"""
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_db
from app.schemas.common import Result
from app.schemas.question import QuestionGenRequest, QuestionGenSaveRequest
from app.security import CurrentUser, require_teacher_or_admin
from app.services import question_gen

router = APIRouter()


@router.post("/generate", response_model=Result)
def generate(req: QuestionGenRequest, db: Session = Depends(get_db),
             user: CurrentUser = Depends(require_teacher_or_admin)):
    """生成题目（预览，不入库）。"""
    data = question_gen.generate(db, req)
    return Result.success(data)


@router.post("/save", response_model=Result)
def save(req: QuestionGenSaveRequest, db: Session = Depends(get_db),
         user: CurrentUser = Depends(require_teacher_or_admin)):
    """确认入库。"""
    saved = question_gen.save(db, req.request, [q.model_dump() for q in req.questions], user.user_id)
    return Result.success(saved, msg=f"入库 {saved} 题")
