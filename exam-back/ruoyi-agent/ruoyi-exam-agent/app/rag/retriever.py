"""RAG 检索

一期策略（架构文档）：**不上向量库**。
题库是结构化数据，文档量在单租户场景下也不大，
用「切片 + 关键词打分」就够用，且零额外依赖、零运维成本。

二期要换向量库时，只需替换 Retriever 的实现，
调用方（Skill / Agent）不受影响。
"""

from __future__ import annotations

import logging
import math
import re
from collections import Counter
from typing import Any

from app.config import settings

logger = logging.getLogger(__name__)

_TOKEN = re.compile(r"[\u4e00-\u9fa5a-zA-Z0-9]+")


def tokenize(text: str) -> list[str]:
    """中英文混合分词：中文按字 bigram，英文按词"""
    if not text:
        return []
    words: list[str] = []
    for seg in _TOKEN.findall(text.lower()):
        if "\u4e00" <= seg[0] <= "\u9fa5":
            # 中文：单字 + bigram，兼顾「数据」与「数据库」
            words.extend(seg)
            words.extend(seg[i : i + 2] for i in range(len(seg) - 1))
        else:
            words.append(seg)
    return words


def split_text(text: str, size: int = 400, overlap: int = 60) -> list[str]:
    """按长度切片，保留重叠避免切断语义"""
    if not text:
        return []
    chunks: list[str] = []
    start = 0
    n = len(text)
    while start < n:
        chunks.append(text[start : start + size])
        start += max(1, size - overlap)
    return chunks


class RagService:
    """内存倒排 + BM25 简化打分"""

    def __init__(self) -> None:
        self._chunks: list[str] = []
        self._meta: list[dict[str, Any]] = []
        self._tf: list[Counter] = []
        self._df: Counter = Counter()
        self._enabled = settings.rag_enabled

    @property
    def enabled(self) -> bool:
        return self._enabled

    def set_enabled(self, flag: bool) -> None:
        self._enabled = flag

    async def ingest(self, doc_key: str, content: str, meta: dict[str, Any] | None = None) -> int:
        if not content:
            return 0
        chunks = split_text(content)
        for c in chunks:
            self._chunks.append(c)
            self._meta.append({"doc_key": doc_key, **(meta or {})})
            tf = Counter(tokenize(c))
            self._tf.append(tf)
            for term in tf:
                self._df[term] += 1
        return len(chunks)

    async def retrieve(self, query: str, top_k: int | None = None) -> str:
        """返回拼接后的上下文文本；未启用或库为空时返回空串"""
        if not self._enabled or not self._chunks:
            return ""
        k = top_k or settings.rag_top_k
        hits = self.search(query, k)
        return "\n\n".join(f"[{i + 1}] {c}" for i, (c, _s, _m) in enumerate(hits))

    def search(self, query: str, top_k: int = 4) -> list[tuple[str, float, dict[str, Any]]]:
        """BM25 简化打分检索"""
        q_terms = Counter(tokenize(query))
        if not q_terms or not self._chunks:
            return []
        n = len(self._chunks)
        scored: list[tuple[int, float]] = []
        for idx, tf in enumerate(self._tf):
            score = 0.0
            for term, qn in q_terms.items():
                f = tf.get(term, 0)
                if not f:
                    continue
                df = self._df.get(term, 0) or 1
                idf = math.log(1 + (n - df + 0.5) / (df + 0.5))
                score += qn * idf * (f * 1.5) / (f + 1.5)
            if score > 0:
                scored.append((idx, score))
        scored.sort(key=lambda x: x[1], reverse=True)
        return [
            (self._chunks[i], round(s, 4), self._meta[i]) for i, s in scored[:top_k]
        ]

    def stats(self) -> dict[str, Any]:
        docs = {m.get("doc_key") for m in self._meta}
        return {
            "enabled": self._enabled,
            "doc_count": len(docs),
            "chunk_count": len(self._chunks),
            "term_count": len(self._df),
        }


rag = RagService()
