"""考试答疑服务：基于 RAG 知识库回答学生问题。"""
import logging

from app.agents.exam_qa import answer, build_qa_chain
from app.services.knowledge_base import knowledge_base

logger = logging.getLogger(__name__)

# 答疑链单例（首次调用时构建）
_qa_chain = None


def chat(message: str) -> str:
    global _qa_chain
    if _qa_chain is None:
        retriever = knowledge_base.get_retriever()
        # 题库为空时检索器为 None，跳过构建答疑链
        _qa_chain = build_qa_chain(retriever) if retriever else False
    if not _qa_chain:
        return "知识库尚未就绪，请稍后再试。"
    return answer(_qa_chain, message)
