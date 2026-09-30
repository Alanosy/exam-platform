"""学情诊断工具：供诊断 Agent 调用，读取学生历史错题与考试记录。"""
from typing import List

from langchain_core.tools import tool
from sqlalchemy.orm import Session

import app.db_models as db_models


def make_diagnosis_tools(db: Session):
    """构造绑定到当前会话的工具集合。"""

    @tool("查询指定学生的历史错题列表，包含题目内容、题型、学生作答与正确答案")
    def list_student_wrong_questions(user_id: int) -> List[dict]:
        return db_models.list_wrong_questions(db, user_id)

    @tool("查询指定学生的历史考试记录，包含考试ID、得分、考试状态")
    def list_student_exam_scores(user_id: int) -> List[dict]:
        return db_models.list_exam_scores(db, user_id)

    return [list_student_wrong_questions, list_student_exam_scores]
