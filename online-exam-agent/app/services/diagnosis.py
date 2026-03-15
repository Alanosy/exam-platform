"""学情诊断服务：调用诊断 Agent 输出薄弱知识点/复习建议/推荐题型。"""
import logging

from sqlalchemy.orm import Session

from app.agents.diagnosis import build_diagnosis_agent, invoke_diagnosis
from app.tools.diagnosis import make_diagnosis_tools

logger = logging.getLogger(__name__)


def diagnose(db: Session, user_id: int) -> dict:
    """对指定学生进行学情诊断。"""
    tools = make_diagnosis_tools(db)
    agent = build_diagnosis_agent(tools)
    return invoke_diagnosis(agent, user_id)
