"""RAG 检索（兼容层）

RAG 已迁移到 app/rag/retriever.py（一期关键词 BM25，不依赖向量库）。
本模块保留导入路径兼容旧代码。
"""

from __future__ import annotations

from app.rag.retriever import RagService, rag


class RagServiceCompat(RagService):
    """兼容旧类名"""


def get_rag() -> RagService:
    return rag


__all__ = ["RagService", "RagServiceCompat", "get_rag", "rag"]
