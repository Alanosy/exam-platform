"""AI 试卷分析接口

对已有试卷进行：难度分布、知识点覆盖、区分度预估、改进建议。
"""

from __future__ import annotations

import logging
from typing import Any

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from app.services.llm import get_llm

logger = logging.getLogger(__name__)
router = APIRouter()


class PaperQuestion(BaseModel):
    question_id: str
    knowledge_points: list[str] = Field(default_factory=list)
    difficulty: str
    score: float
    question_type: str


class PaperAnalyzeRequest(BaseModel):
    paper_id: str
    title: str | None = None
    questions: list[PaperQuestion]
    extra: dict[str, Any] = Field(default_factory=dict)


class PaperAnalyzeResponse(BaseModel):
    code: int = 200
    msg: str = "success"
    data: dict


@router.post("/analyze", response_model=PaperAnalyzeResponse)
async def analyze_paper(req: PaperAnalyzeRequest) -> PaperAnalyzeResponse:
    """试卷难度与覆盖度分析"""
    try:
        llm = get_llm()
        result = await llm.analyze_paper(
            paper_id=req.paper_id,
            title=req.title,
            questions=[q.model_dump() for q in req.questions],
            extra=req.extra,
        )
        return PaperAnalyzeResponse(data=result)
    except Exception as e:  # noqa: BLE001
        logger.exception("试卷分析失败: %s", e)
        raise HTTPException(status_code=500, detail=f"试卷分析失败: {e}") from e
