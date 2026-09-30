"""RAG 知识库检索封装

默认使用内存向量库（Chroma in-memory），可替换为 Milvus / pgvector。
文档由 Java 端上传到 ruoyi-resource 后，异步同步到本服务。
"""

from __future__ import annotations

import logging
from typing import Any

from app.config import settings

logger = logging.getLogger(__name__)

_rag: "RagService | None" = None


def get_rag() -> "RagService":
    global _rag
    if _rag is None:
        _rag = RagService()
    return _rag


class RagService:
    """RAG 检索服务（骨架）

    生产环境建议：
    1. 用 ChromaPersistentClient 或 Milvus 持久化向量
    2. 在 Nacos 配置里切换 embedding 模型（bge-m3 / text-embedding-3）
    3. 文档切片由 ruoyi-exam-ai 异步任务完成后调用 /api/ai/rag/ingest
    """

    def __init__(self) -> None:
        self._enabled = settings.rag_enabled
        self._store: dict[str, str] = {}  # 占位，正式接入向量库后替换

    async def ingest(self, doc_key: str, content: str, meta: dict[str, Any] | None = None) -> int:
        if not self._enabled:
            return 0
        self._store[doc_key] = content
        return 1

    async def retrieve(self, query: str, top_k: int = 4) -> str:
        if not self._enabled:
            return ""
        # 骨架：实际应走向量相似度检索，这里直接返回空串
        logger.info("RAG retrieve query=%s top_k=%d", query, top_k)
        return ""
