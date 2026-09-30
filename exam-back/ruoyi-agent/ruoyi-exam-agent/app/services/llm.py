"""LLM 调用封装

设计要点：
- 通过 LangChain 抽象，后端可切换通义千问 / DeepSeek / OpenAI / 自托管 vLLM
- 出题、阅卷、推荐、试卷分析 走不同 prompt 模板
- 阅卷返回结构化 JSON（评分点 + 置信度），便于 Java 端审计
"""

from __future__ import annotations

import json
import logging
from typing import Any

from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_openai import ChatOpenAI

from app.api.question import GeneratedQuestion
from app.api.grade import GradeResultItem
from app.api.recommend import RecommendItem
from app.config import settings

logger = logging.getLogger(__name__)

_llm: ChatOpenAI | None = None


def get_llm() -> "LlmService":
    """获取 LLM 服务单例"""
    global _llm
    if _llm is None:
        _llm = ChatOpenAI(
            model=settings.llm_model,
            api_key=settings.llm_api_key,
            base_url=settings.llm_base_url,
            temperature=settings.llm_temperature,
            request_timeout=settings.llm_timeout,
        )
    return LlmService(_llm)


class LlmService:
    def __init__(self, llm: ChatOpenAI) -> None:
        self.llm = llm

    async def generate_questions(
        self,
        knowledge_points: list[str],
        difficulty: str,
        question_type: str,
        count: int,
        rag_context: str,
        extra: dict[str, Any],
    ) -> list[GeneratedQuestion]:
        prompt = ChatPromptTemplate.from_template(
            "你是出题专家。请按以下约束生成 {count} 道题目。\n"
            "题型：{question_type}\n"
            "难度：{difficulty}\n"
            "知识点：{knowledge_points}\n"
            "参考资料：{rag_context}\n"
            "附加约束：{extra}\n\n"
            "请严格输出 JSON 数组，每个元素字段：question_type, stem, options, answer, analysis, knowledge_points, difficulty。"
        )
        chain = prompt | self.llm | StrOutputParser()
        raw = await chain.ainvoke(
            {
                "count": count,
                "question_type": question_type,
                "difficulty": difficulty,
                "knowledge_points": knowledge_points,
                "rag_context": rag_context or "(无)",
                "extra": json.dumps(extra, ensure_ascii=False),
            }
        )
        data = _safe_json_loads(raw, default=[])
        return [GeneratedQuestion(**item) for item in data]

    async def grade_answers(
        self,
        paper_id: str,
        answer_sheet_id: str,
        items: list[dict],
    ) -> list[GradeResultItem]:
        prompt = ChatPromptTemplate.from_template(
            "你是阅卷专家。请对以下作答逐题评分，严格输出 JSON 数组。\n"
            "每项字段：question_id, score, matched_rubric, comment, confidence。\n"
            "评分点：{items}\n"
        )
        chain = prompt | self.llm | StrOutputParser()
        raw = await chain.ainvoke({"items": json.dumps(items, ensure_ascii=False)})
        data = _safe_json_loads(raw, default=[])
        return [GradeResultItem(**item) for item in data]

    async def recommend_questions(
        self,
        user_id: str,
        wrong_question_ids: list[str],
        mastered_points: dict[str, float],
        target_count: int,
        extra: dict[str, Any],
    ) -> list[RecommendItem]:
        prompt = ChatPromptTemplate.from_template(
            "根据考生错题与掌握度，推荐 {target_count} 道练习题。\n"
            "错题：{wrong_question_ids}\n"
            "掌握度：{mastered_points}\n"
            "严格输出 JSON 数组，字段：question_id, reason, priority(1-10)。"
        )
        chain = prompt | self.llm | StrOutputParser()
        raw = await chain.ainvoke(
            {
                "target_count": target_count,
                "wrong_question_ids": wrong_question_ids,
                "mastered_points": json.dumps(mastered_points, ensure_ascii=False),
            }
        )
        data = _safe_json_loads(raw, default=[])
        return [RecommendItem(**item) for item in data]

    async def analyze_paper(
        self,
        paper_id: str,
        title: str | None,
        questions: list[dict],
        extra: dict[str, Any],
    ) -> dict:
        prompt = ChatPromptTemplate.from_template(
            "你是试卷分析专家。请分析试卷：{title}\n"
            "题目列表：{questions}\n"
            "输出 JSON：{{ \"difficulty_distribution\": {{}}, "
            "\"knowledge_coverage\": [], \"suggestions\": [] }}。"
        )
        chain = prompt | self.llm | StrOutputParser()
        raw = await chain.ainvoke(
            {
                "title": title or paper_id,
                "questions": json.dumps(questions, ensure_ascii=False),
            }
        )
        return _safe_json_loads(raw, default={})


def _safe_json_loads(text: str, default: Any) -> Any:
    """LLM 输出可能带 markdown 代码块，做容错解析"""
    try:
        cleaned = text.strip()
        if cleaned.startswith("```"):
            cleaned = cleaned.split("```", 2)[1]
            if cleaned.startswith("json"):
                cleaned = cleaned[4:]
        return json.loads(cleaned)
    except Exception as e:  # noqa: BLE001
        logger.warning("LLM JSON 解析失败: %s, 原文: %s", e, text[:200])
        return default
