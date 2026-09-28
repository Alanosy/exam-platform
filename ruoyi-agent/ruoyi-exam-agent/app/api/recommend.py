"""AI 错题推荐 / 个性化推题接口

输入：考生错题历史 + 知识点掌握度
输出：按艾宾浩斯曲线 + LLM 重排序后的推荐题目
"""

from __future__ import annotations

import logging
from typing import Any

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from app.services.llm import get_llm

logger = logging.getLogger(__name__)
router = APIRouter()


class RecommendRequest(BaseModel):
    user_id: str
    wrong_question_ids: list[str] = Field(default_factory=list)
    mastered_points: dict[str, float] = Field(
        default_factory=dict, description="知识点 -> 掌握度 0~1"
    )
    target_count: int = Field(default=10, ge=1, le=50)
    tenant_id: str | None = None
    extra: dict[str, Any] = Field(default_factory=dict)


class RecommendItem(BaseModel):
    question_id: str
    reason: str
    priority: int = Field(..., ge=1, le=10)


class RecommendResponse(BaseModel):
    code: int = 200
    msg: str = "success"
    data: list[RecommendItem]


@router.post("/wrong", response_model=RecommendResponse)
async def recommend_for_wrong(req: RecommendRequest) -> RecommendResponse:
    """基于错题本生成推荐"""
    try:
        llm = get_llm()
        items = await llm.recommend_questions(
            user_id=req.user_id,
            wrong_question_ids=req.wrong_question_ids,
            mastered_points=req.mastered_points,
            target_count=req.target_count,
            extra=req.extra,
        )
        return RecommendResponse(data=items)
    except Exception as e:  # noqa: BLE001
        logger.exception("AI 推荐失败: %s", e)
        raise HTTPException(status_code=500, detail=f"AI 推荐失败: {e}") from e
