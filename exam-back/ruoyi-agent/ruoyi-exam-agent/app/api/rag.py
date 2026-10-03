"""RAG 知识库接口

一期不上向量库：切片 + 关键词 BM25 打分，零额外依赖。
文档由 Java 侧（ruoyi-resource）上传后调用 /ingest 同步过来。
"""

from __future__ import annotations

import logging

from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.core.response import ok
from app.rag.retriever import rag

logger = logging.getLogger(__name__)
router = APIRouter()


class IngestRequest(BaseModel):
    doc_key: str
    content: str
    meta: dict = Field(default_factory=dict)


class RetrieveRequest(BaseModel):
    query: str
    top_k: int = Field(default=4, ge=1, le=20)


@router.post("/rag/ingest")
async def ingest(req: IngestRequest) -> dict:
    count = await rag.ingest(req.doc_key, req.content, req.meta)
    return ok({"doc_key": req.doc_key, "chunks": count, "stats": rag.stats()})


@router.post("/rag/retrieve")
async def retrieve(req: RetrieveRequest) -> dict:
    ctx = await rag.retrieve(req.query, req.top_k)
    return ok({"query": req.query, "context": ctx, "has_context": bool(ctx)})


@router.get("/rag/stats")
async def stats() -> dict:
    return ok(rag.stats())


@router.post("/rag/enable")
async def enable(flag: bool = True) -> dict:
    rag.set_enabled(flag)
    return ok({"enabled": rag.enabled})
