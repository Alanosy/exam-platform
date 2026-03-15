"""智能出题服务：生成 -> 服务端去重 -> 预览 -> 入库。"""
import logging

from sqlalchemy.orm import Session

import app.db_models as db_models
from app.agents.question_gen import build_question_gen_agent, invoke_question_gen
from app.schemas.question import GeneratedOption, QuestionGenRequest
from app.tools.question_bank import make_question_bank_tools

logger = logging.getLogger(__name__)


def generate(db: Session, req: QuestionGenRequest) -> list[dict]:
    """调用出题 Agent 生成题目，服务端二次去重，返回预览列表。"""
    tools = make_question_bank_tools(db)
    agent = build_question_gen_agent(tools)

    raw = invoke_question_gen(
        agent, req.repoId, req.knowledgePoints, req.difficulty, req.quType, req.count
    )

    # 服务端二次去重
    existing = set(db_models.list_existing_question_contents(db, req.repoId, req.quType))
    for item in raw:
        content = (item.get("content") or "").strip()
        item["duplicated"] = bool(content) and content in existing

    return raw


def save(db: Session, req: QuestionGenRequest, questions: list[dict], user_id: int) -> int:
    """将选中的非重复题目入库到 t_question / t_option。"""
    saved = 0
    for q in questions:
        if q.get("duplicated"):
            continue
        qid = db_models.insert_question(
            db,
            qu_type=req.quType,
            content=q["content"],
            analysis=q.get("analysis"),
            repo_id=req.repoId,
            user_id=user_id,
            image=q.get("image"),
        )
        for idx, opt in enumerate(q.get("options") or []):
            db_models.insert_option(
                db,
                qu_id=qid,
                is_right=opt.get("isRight", 0),
                content=opt.get("content", ""),
                sort=opt.get("sort") if opt.get("sort") is not None else idx,
            )
        saved += 1
    db.commit()
    return saved
