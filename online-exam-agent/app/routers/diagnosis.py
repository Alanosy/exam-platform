"""学情诊断 Agent 接口。"""
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_db
from app.schemas.common import Result
from app.security import CurrentUser, require_student
from app.services import diagnosis

router = APIRouter()


@router.get("/analyze", response_model=Result)
def analyze(db: Session = Depends(get_db), user: CurrentUser = Depends(require_student)):
    """学生学情诊断。"""
    data = diagnosis.diagnose(db, user.user_id)
    return Result.success(data)
