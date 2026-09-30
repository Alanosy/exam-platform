"""学情诊断 Agent：基于 LangGraph ReAct，可调用错题/考试记录工具，
输出薄弱知识点、复习建议、推荐题型的结构化 JSON。"""
import json
import re

from langgraph.prebuilt import create_react_agent

from app.agents.llm import get_chat_model

SYSTEM_PROMPT = """你是学情诊断 Agent。请先调用工具读取该学生的历史错题与考试记录，
然后分析其薄弱知识点，生成个性化复习建议，并推荐需要重点练习的题型。

严格输出 JSON（不要 markdown 代码块、不要多余解释），格式如下：
{
  "weakPoints": [
    {"knowledgePoint": "知识点", "wrongCount": 3, "analysis": "问题分析"}
  ],
  "reviewSuggestions": ["建议1", "建议2"],
  "recommendTypes": [
    {"quType": 1, "quTypeName": "单选题", "reason": "推荐原因"}
  ]
}"""


def build_diagnosis_agent(tools):
    """构造可调用诊断工具的学情诊断 Agent。"""
    return create_react_agent(get_chat_model(), tools, state_modifier=SYSTEM_PROMPT)


def invoke_diagnosis(agent, user_id: int) -> dict:
    """调用诊断 Agent，返回解析后的诊断结果字典。"""
    user_msg = (
        f"请对学生用户ID={user_id} 进行学情诊断。"
        f"先调用工具查询其历史错题与考试记录，再分析薄弱知识点、生成复习建议、推荐练习题型。"
    )
    result = agent.invoke({"messages": [("user", user_msg)]})
    final = result["messages"][-1].content
    return _parse_json(final)


def _parse_json(text: str) -> dict:
    m = re.search(r"```(?:json)?\s*([\s\S]*?)```", text)
    candidate = m.group(1).strip() if m else text.strip()
    if "{" in candidate and "}" in candidate:
        lb = candidate.find("{")
        rb = candidate.rfind("}")
        if rb > lb:
            candidate = candidate[lb:rb + 1]
    try:
        return json.loads(candidate)
    except Exception:
        return {"weakPoints": [], "reviewSuggestions": [], "recommendTypes": []}
