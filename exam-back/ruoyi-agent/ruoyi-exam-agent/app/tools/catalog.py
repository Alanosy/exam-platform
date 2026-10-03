"""工具清单

每个工具对应 Java 侧的一项业务能力（最终落到 Remote*Service）。
Agent 在 ReAct / Plan-and-Execute 循环里调用它们。
"""

from __future__ import annotations

from app.tools.base import READ, WRITE, ToolSpec

TOOLS: dict[str, ToolSpec] = {
    # ---------------- 题库 ----------------
    "list_question_banks": ToolSpec(
        code="list_question_banks",
        name="题库列表",
        description="按名称关键词模糊查询题库（带题目数量），用于定位「往哪个题库写」",
        path="/api/exam-tool/bank/list",
        risk_level=READ,
        params={"keyword": "str?", "limit": "int=20"},
    ),
    "create_question_bank": ToolSpec(
        code="create_question_bank",
        name="新建题库",
        description="创建一个新的题库（写操作，对话场景由用户确认后才会调用）",
        path="/api/exam-tool/bank/create",
        risk_level=WRITE,
        params={"bankName": "str", "categoryId": "str?"},
    ),
    "search_questions": ToolSpec(
        code="search_questions",
        name="检索试题",
        description="按知识点/题型/难度/关键词检索题库中的试题",
        path="/api/exam-tool/question/search",
        risk_level=READ,
        params={"bank_id": "str?", "question_type": "str?", "difficulty": "str?", "keyword": "str?", "limit": "int=20"},
    ),
    "get_question": ToolSpec(
        code="get_question",
        name="查询单题",
        description="按 ID 查询试题详情（含选项与答案）",
        path="/api/exam-tool/question/get",
        risk_level=READ,
        params={"question_id": "str"},
    ),
    "save_question": ToolSpec(
        code="save_question",
        name="保存试题",
        description="把 AI 生成的试题写入题库（草稿态，需人工审核）",
        path="/api/exam-tool/question/save",
        risk_level=WRITE,
        params={"question": "object"},
    ),
    # ---------------- 试卷 ----------------
    "get_paper": ToolSpec(
        code="get_paper",
        name="查询试卷",
        description="按 ID 查询试卷与题目清单",
        path="/api/exam-tool/paper/get",
        risk_level=READ,
        params={"paper_id": "str"},
    ),
    "save_questions": ToolSpec(
        code="save_questions",
        name="批量保存试题",
        description="把 AI 生成的试题批量写入指定题库（含选项与答案）",
        path="/api/exam-tool/question/save-batch",
        risk_level=WRITE,
        params={"bankId": "str", "status": "str=0", "questions": "list[object]"},
    ),
    "add_paper_questions": ToolSpec(
        code="add_paper_questions",
        name="试卷加题",
        description="向试卷加入题目",
        path="/api/exam-tool/paper/add-questions",
        risk_level=WRITE,
        params={"paper_id": "str", "question_ids": "list[str]"},
    ),
    # ---------------- 考试 ----------------
    "get_exam": ToolSpec(
        code="get_exam",
        name="查询考试",
        description="按 ID 查询考试配置（含防作弊配置）",
        path="/api/exam-tool/exam/get",
        risk_level=READ,
        params={"exam_id": "str"},
    ),
    "find_exam": ToolSpec(
        code="find_exam",
        name="查找考试",
        description="按名称关键词模糊查询考试（对话场景用于定位用户口中的「某某考卷」）",
        path="/api/exam-tool/exam/find",
        risk_level=READ,
        params={"keyword": "str", "limit": "int=10"},
    ),
    "publish_exam": ToolSpec(
        code="publish_exam",
        name="发布考试",
        description="发布考试（高危：考生可见，必须人工确认）",
        path="/api/exam-tool/exam/publish",
        risk_level=WRITE,
        params={"exam_id": "str"},
    ),
    # ---------------- 阅卷 ----------------
    "list_pending_mark": ToolSpec(
        code="list_pending_mark",
        name="待阅清单",
        description="列出某场考试待阅的主观题明细",
        path="/api/exam-tool/mark/list-pending",
        risk_level=READ,
        params={"exam_id": "str", "limit": "int=50"},
    ),
    "get_mark_item": ToolSpec(
        code="get_mark_item",
        name="阅卷明细",
        description="查询单条阅卷明细（题干、参考答案、考生作答）",
        path="/api/exam-tool/mark/get-item",
        risk_level=READ,
        params={"item_id": "str"},
    ),
    "submit_mark_score": ToolSpec(
        code="submit_mark_score",
        name="提交分数",
        description="写入最终分数（高危：直接影响考生成绩，必须人工确认）",
        path="/api/exam-tool/mark/submit-score",
        risk_level=WRITE,
        params={"item_id": "str", "score": "number", "comment": "str?"},
    ),
    "save_ai_score": ToolSpec(
        code="save_ai_score",
        name="保存 AI 建议分",
        description="写入 AI 建议分（不覆盖人工分，落 ai_score 字段）",
        path="/api/exam-tool/mark/save-ai-score",
        risk_level=WRITE,
        params={"item_id": "str", "ai_score": "number", "ai_reason": "str", "ai_model": "str"},
    ),
    # ---------------- 统计 ----------------
    "exam_stats": ToolSpec(
        code="exam_stats",
        name="考试统计",
        description="查询某场考试的参考人数、平均分、及格率等",
        path="/api/exam-tool/stat/exam",
        risk_level=READ,
        params={"exam_id": "str"},
    ),
    "exam_answer_stats": ToolSpec(
        code="exam_answer_stats",
        name="考试答卷统计",
        description="统计某场考试的参考人数、交卷数、平均分、及格率与分数段分布",
        path="/api/exam-tool/stat/exam-answers",
        risk_level=READ,
        params={"examId": "str"},
    ),
    "question_stats": ToolSpec(
        code="question_stats",
        name="题目统计",
        description="查询某道题的正确率与作答分布",
        path="/api/exam-tool/stat/question",
        risk_level=READ,
        params={"question_id": "str", "exam_id": "str?"},
    ),
    # ---------------- 练习 ----------------
    "list_wrong_questions": ToolSpec(
        code="list_wrong_questions",
        name="错题列表",
        description="查询某考生的错题本",
        path="/api/exam-tool/practice/wrong-list",
        risk_level=READ,
        params={"user_id": "str", "limit": "int=50"},
    ),
    # ---------------- 监考 ----------------
    "list_proctor_events": ToolSpec(
        code="list_proctor_events",
        name="监考事件",
        description="查询某场考试/某考生的监考事件时序",
        path="/api/exam-tool/proctor/events",
        risk_level=READ,
        params={"exam_id": "str", "user_id": "str?"},
    ),
    # ---------------- 通用：系统接口（MCP 化的那部分能力） ----------------
    # 下面三个是「通用出口」：预置工具覆盖不到的问题，用它们去查系统里真实的数据，
    # 而不必为每个需求都新写一个工具。
    "api_manifest": ToolSpec(
        code="api_manifest",
        name="系统能力清单",
        description="列出考试域所有可代调的接口（路径/入参/语义/所需权限），规划前先看它",
        path="/api/exam-tool/api/manifest",
        risk_level=READ,
        params={},
    ),
    "api_call": ToolSpec(
        code="api_call",
        name="调用系统接口",
        description="以当前登录用户的身份调用一个系统接口（带用户令牌，权限由网关判定，"
                    "越权会返回 403）。用于查数据、做统计；写操作必须先拿到用户确认。",
        path="/api/exam-tool/api/call",
        risk_level=READ,
        params={
            "method": "str（GET/POST/PUT/DELETE）",
            "path": "str（如 /exam/list）",
            "query": "object?（GET 参数）",
            "body": "object?（非 GET 的请求体）",
        },
    ),
    "whoami": ToolSpec(
        code="whoami",
        name="查询用户身份",
        description="查询当前用户的角色与考试域权限，用于判断「这件事他有没有权限做」",
        path="/api/exam-tool/whoami",
        risk_level=READ,
        params={"userId": "str"},
    ),
}


def get_tool(code: str) -> ToolSpec | None:
    return TOOLS.get(code)


def list_tools() -> list[dict[str, object]]:
    return [t.info() for t in TOOLS.values()]


__all__ = ["TOOLS", "get_tool", "list_tools", "READ", "WRITE"]
