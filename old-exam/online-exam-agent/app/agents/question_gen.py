"""智能出题 Agent：基于 LangGraph ReAct，可调用题库工具读取已有题目去重，
最终输出结构化题目 JSON 数组。"""
import json

from langgraph.prebuilt import create_react_agent

from app.agents.llm import get_chat_model

SYSTEM_PROMPT = """你是智能出题 Agent。请按以下要求生成题目：

1. 先调用工具查询指定题库与题型下的已有题目题干，避免生成重复题目；
2. 根据知识点、难度、题型、题量生成新题目；
3. 题型编号：1单选题、2多选题、3判断题、4简答题；
4. 单选/多选/判断题需给出 options 列表（isRight 0错误/1正确）；
5. 简答题给出 answer（标准答案文本）；
6. 每题可选给出 analysis（解析）。

严格输出 JSON 数组（不要 markdown 代码块、不要多余解释），每个元素结构如下：
{
  "quType": 1,
  "content": "题干",
  "analysis": "解析",
  "answer": "简答题标准答案，客观题为空",
  "options": [{"isRight": 1, "content": "选项内容"}, {"isRight": 0, "content": "选项内容"}]
}
"""


def build_question_gen_agent(tools):
    """构造可调用题库工具的出题 Agent。

    :param tools: 由 make_question_bank_tools 生成，绑定到当前数据库会话。
    """
    return create_react_agent(get_chat_model(), tools, state_modifier=SYSTEM_PROMPT)


def invoke_question_gen(agent, repo_id: int, knowledge_points: str, difficulty: str,
                        qu_type: int, count: int) -> list[dict]:
    """调用出题 Agent 并解析最终输出为题目列表。"""
    user_msg = (
        f"题库ID：{repo_id}；知识点：{knowledge_points}；难度：{difficulty}；"
        f"题型编号：{qu_type}；题量：{count}。\n"
        f"请先调用工具查询题库ID为{repo_id}、题型编号为{qu_type}的已有题目题干，"
        f"避免生成重复题目，再生成符合要求的题目，按系统约定的 JSON 数组格式输出。"
    )
    result = agent.invoke({"messages": [("user", user_msg)]})
    final = result["messages"][-1].content
    return _parse_json_array(final)


def _parse_json_array(text: str) -> list[dict]:
    """从模型输出中提取 JSON 数组（兼容 markdown 代码块）。"""
    import re

    m = re.search(r"```(?:json)?\s*([\s\S]*?)```", text)
    candidate = m.group(1).strip() if m else text.strip()
    # 兜底：截取首个 [ 到 ] 的片段
    if "[" in candidate and "]" in candidate:
        lb = candidate.find("[")
        rb = candidate.rfind("]")
        if rb > lb:
            candidate = candidate[lb:rb + 1]
    try:
        data = json.loads(candidate)
        if isinstance(data, list):
            return data
        return [data]
    except Exception:
        return []
