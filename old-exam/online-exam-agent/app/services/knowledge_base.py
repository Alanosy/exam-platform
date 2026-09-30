"""知识库加载服务：启动/首次访问时把题库向量化写入 FAISS 内存向量库，
供考试答疑 Agent 做 RAG 检索。"""
import logging
import threading
from typing import Optional

from langchain_community.vectorstores import FAISS
from langchain_core.documents import Document
from langchain_openai import OpenAIEmbeddings

from app.config import settings
from app.database import SessionLocal
import app.db_models as db_models

logger = logging.getLogger(__name__)

_TYPE_NAMES = {1: "单选题", 2: "多选题", 3: "判断题", 4: "简答题"}


class KnowledgeBase:
    """题库向量库（单例，懒加载）。"""

    def __init__(self):
        self._store: Optional[FAISS] = None
        self._lock = threading.Lock()
        self._loaded = False

    def warmup(self) -> None:
        """启动时触发一次构建（不阻塞主流程：在后台线程执行）。"""
        if self._loaded:
            return
        threading.Thread(target=self._build, daemon=True).start()

    def get_retriever(self, k: Optional[int] = None):
        """获取检索器；若尚未构建则同步构建。"""
        if not self._loaded:
            self._build()
        if self._store is None:
            # 题库为空时返回空检索器
            return None
        return self._store.as_retriever(
            search_type="similarity_score_threshold",
            search_kwargs={
                "k": k or settings.rag_max_results,
                "score_threshold": settings.rag_min_score,
            },
        )

    def _build(self) -> None:
        with self._lock:
            if self._loaded:
                return
            try:
                self._store = self._load_from_db()
                self._loaded = True
                logger.info("知识库加载完成")
            except Exception:
                logger.exception("知识库加载失败")
                self._loaded = True  # 避免反复重试

    def _load_from_db(self) -> Optional[FAISS]:
        db = SessionLocal()
        try:
            repos = db_models.list_all_repos(db)
            repo_name = {r["id"]: r["title"] for r in repos}

            questions = db_models.list_all_questions(db)
            if not questions:
                logger.warning("题库为空，跳过知识库加载")
                return None

            options = db_models.list_all_options(db)
            opt_buf: dict[int, list[str]] = {}
            for o in options:
                opt_buf.setdefault(o["qu_id"], []).append(o["content"])

            docs = []
            for q in questions:
                text = self._build_text(q, opt_buf.get(q["id"]), repo_name.get(q["repo_id"]))
                docs.append(Document(page_content=text))

            embeddings = OpenAIEmbeddings(
                api_key=settings.embedding_api_key,
                base_url=settings.embedding_base_url,
                model=settings.embedding_model,
            )
            store = FAISS.from_documents(docs, embeddings)
            logger.info("向量化完成：共 %d 道题目", len(docs))
            return store
        finally:
            db.close()

    @staticmethod
    def _build_text(q: dict, opts: Optional[list[str]], repo_title: Optional[str]) -> str:
        parts = [f"题型：{_TYPE_NAMES.get(q['qu_type'], '未知')}。"]
        if repo_title:
            parts.append(f"题库：{repo_title}。")
        parts.append(f"题干：{q['content']}。")
        if opts:
            parts.append("选项：" + "; ".join(opts) + "。")
        if q.get("analysis"):
            parts.append(f"解析：{q['analysis']}。")
        return "".join(parts)


knowledge_base = KnowledgeBase()
