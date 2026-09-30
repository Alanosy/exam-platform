"""AI 自动阅卷接口

对主观题（简答 / 论述 / 计算）进行语义评分，返回得分与评分点勾选。
Java 阅卷服务 ruoyi-exam-mark 在收到 AI 阅卷任务后通过网关调用本接口。
"""

from __future__ import annotations

import logging
from typing import Any

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from app.services.llm import get_llm

logger = logging.getLogger(__name__)
router = APIRouter()


class GradeItem(BaseModel):
    """单题阅卷请求"""

    question_id: str
    question_type: str = Field(..., description="short / essay / calculation")
    stem: str
    reference_answer: str
    rubric: list[str] | None = Field(default=None, description="评分点列表")
    student_answer: str
    full_score: float = Field(..., ge=0)
    extra: dict[str, Any] = Field(default_factory=dict)


class GradeBatchRequest(BaseModel):
    paper_id: str
    answer_sheet_id: str
    items: list[GradeItem]


class GradeResultItem(BaseModel):
    question_id: str
    score: float
    matched_rubric: list[str] = Field(default_factory=list)
    comment: str | None = None
    confidence: float = Field(..., ge=0, le=1)


class GradeBatchResponse(BaseModel):
    code: int = 200
    msg: str = "success"
    data: list[GradeResultItem]


@router.post("/auto", response_model=GradeBatchResponse)
async def auto_grade(req: GradeBatchRequest) -> GradeBatchResponse:
    """AI 批量阅卷"""
    try:
        llm = get_llm()
        results = await llm.grade_answers(
            paper_id=req.paper_id,
            answer_sheet_id=req.answer_sheet_id,
            items=[item.model_dump() for item in req.items],
        )
        return GradeBatchResponse(data=results)
    except Exception as e:  # noqa: BLE001
        logger.exception("AI 阅卷失败: %s", e)
        raise HTTPException(status_code=500, detail=f"AI 阅卷失败: {e}") from e
