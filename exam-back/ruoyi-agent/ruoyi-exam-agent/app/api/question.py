"""AI 出题接口

Java 网关 ruoyi-exam-ai 调用本接口完成：
- 按知识点 / 难度 / 题型自动生成题目
- 基于材料/讲义 RAG 检索后生成
- 批量生成与去重
"""

from __future__ import annotations

import logging
from typing import Any

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from app.services.llm import get_llm
from app.services.rag import get_rag

logger = logging.getLogger(__name__)
router = APIRouter()


class QuestionGenerateRequest(BaseModel):
    """出题请求"""

    knowledge_points: list[str] = Field(default_factory=list, description="知识点列表")
    difficulty: str = Field(default="medium", description="easy / medium / hard")
    question_type: str = Field(..., description="single / multiple / judge / fill / short")
    count: int = Field(default=1, ge=1, le=20, description="生成数量")
    material_key: str | None = Field(default=None, description="RAG 知识库文档 key，可选")
    tenant_id: str | None = Field(default=None, description="租户 ID")
    extra: dict[str, Any] = Field(default_factory=dict, description="附加约束，如分值、解析语言")


class GeneratedQuestion(BaseModel):
    """单道生成题目"""

    question_type: str
    stem: str
    options: list[str] | None = None
    answer: str | list[str] | None = None
    analysis: str | None = None
    knowledge_points: list[str] | None = None
    difficulty: str | None = None


class QuestionGenerateResponse(BaseModel):
    code: int = 200
    msg: str = "success"
    data: list[GeneratedQuestion]


@router.post("/generate", response_model=QuestionGenerateResponse)
async def generate_questions(req: QuestionGenerateRequest) -> QuestionGenerateResponse:
    """根据约束生成题目"""
    try:
        rag_context = ""
        if req.material_key:
            rag_context = await get_rag().retrieve(req.material_key, top_k=4)

        llm = get_llm()
        questions = await llm.generate_questions(
            knowledge_points=req.knowledge_points,
            difficulty=req.difficulty,
            question_type=req.question_type,
            count=req.count,
            rag_context=rag_context,
            extra=req.extra,
        )
        return QuestionGenerateResponse(data=questions)
    except Exception as e:  # noqa: BLE001
        logger.exception("AI 出题失败: %s", e)
        raise HTTPException(status_code=500, detail=f"AI 出题失败: {e}") from e
