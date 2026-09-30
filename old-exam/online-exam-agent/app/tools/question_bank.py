"""智能出题工具：供出题 Agent 调用，读取题库已有题目用于去重。

LangChain @tool 通过工具签名感知参数；数据库会话在调用前注入。
"""
from typing import List

from langchain_core.tools import tool
from sqlalchemy.orm import Session

import app.db_models as db_models


def make_question_bank_tools(db: Session):
    """构造绑定到当前会话的工具集合。"""

    @tool("查询指定题库与题型下已有题目的题干列表，用于避免生成重复题目")
    def list_existing_questions(repo_id: int, qu_type: int) -> List[str]:
        """输入题库ID与题型编号(1单选2多选3判断4简答)，返回已有题目题干列表。"""
        return db_models.list_existing_question_contents(db, repo_id, qu_type)

    return [list_existing_questions]
