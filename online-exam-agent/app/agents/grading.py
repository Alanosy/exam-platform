"""主观题批改 Agent：使用 ChatOpenAI 结构化输出，逐题输出得分/得分点/错误分析。

不使用工具，直接把待批改题目列表喂给 LLM，要求按 Pydantic 模型输出。
"""
from typing import List

from pydantic import BaseModel, Field

from app.agents.llm import get_chat_model
from app.schemas.grading import GradingResult


class GradingResultList(BaseModel):
    """批改结果列表（结构化输出容器，确保 LLM 输出稳定）"""
    results: List[GradingResult] = Field(default_factory=list, description="逐题批改结果")


SYSTEM_PROMPT = """你是主观题批改 Agent。请根据题目内容、题目总分、标准答案与学生作答进行评分。

要求：
1. 按题目总分给出得分点（scoringPoints）与错误分析（errorAnalysis）；
2. score 为最终得分，取值区间 [0, 题目总分]；
3. 严格按给定结构输出，不要输出多余解释。"""


def grade_questions(items: list[dict]) -> list[GradingResult]:
    """对一批主观题进行 AI 批改，返回结构化结果。

    :param items: 来自 get_questions_for_grading 的字典列表，
                  含 questionId/questionContent/totalScore/standardAnswer/userAnswer。
    """
    if not items:
        return []

    structured = get_chat_model().with_structured_output(GradingResultList)
    user_msg = (
        "以下是待批改的主观题列表（JSON 数组），请逐题评分：\n"
        + _to_json(items)
    )
    result = structured.invoke([
        ("system", SYSTEM_PROMPT),
        ("user", user_msg),
    ])
    return result.results if result and result.results else []


def _to_json(items: list[dict]) -> str:
    import json
    safe = []
    for it in items:
        safe.append({
            "questionId": it.get("questionId"),
            "questionContent": it.get("questionContent"),
            "totalScore": it.get("totalScore"),
            "standardAnswer": it.get("standardAnswer"),
            "userAnswer": it.get("userAnswer"),
        })
    return json.dumps(safe, ensure_ascii=False)
