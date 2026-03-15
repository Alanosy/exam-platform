"""SQLAlchemy Core 表定义 + 数据访问查询（对应 Java 版 Mapper SQL）。

直连同库 db_exam，复用考试系统表结构。
"""
from __future__ import annotations

from datetime import datetime
from typing import Optional

from sqlalchemy import (
    Column,
    DateTime,
    Integer,
    MetaData,
    String,
    Table,
    Text,
    and_,
    select,
    text,
    update,
)
from sqlalchemy.orm import Session

metadata = MetaData()

# ===== 表定义（只声明 Agent 需要的字段）=====

t_question = Table(
    "t_question", metadata,
    Column("id", Integer, primary_key=True),
    Column("qu_type", Integer),          # 1单选 2多选 3判断 4简答
    Column("image", String(255)),
    Column("content", Text),
    Column("create_time", DateTime),
    Column("analysis", Text),
    Column("repo_id", Integer),
    Column("user_id", Integer),
    Column("is_deleted", Integer),
)

t_option = Table(
    "t_option", metadata,
    Column("id", Integer, primary_key=True),
    Column("qu_id", Integer),
    Column("is_right", Integer),         # 0错误 1正确
    Column("image", String(255)),
    Column("content", Text),
    Column("sort", Integer),
    Column("is_deleted", Integer),
)

t_repo = Table(
    "t_repo", metadata,
    Column("id", Integer, primary_key=True),
    Column("user_id", Integer),
    Column("title", String(255)),
    Column("category_id", Integer),
    Column("create_time", DateTime),
    Column("is_deleted", Integer),
    Column("is_exercise", Integer),
)

t_exam_qu_answer = Table(
    "t_exam_qu_answer", metadata,
    Column("id", Integer, primary_key=True),
    Column("user_id", Integer),
    Column("exam_id", Integer),
    Column("question_id", Integer),
    Column("question_type", Integer),
    Column("answer_id", String(255)),
    Column("answer_content", Text),
    Column("checkout", Integer),
    Column("is_sign", Integer),
    Column("is_right", Integer),
    Column("ai_score", Integer),
    Column("ai_reason", String(255)),
)

t_exam_question = Table(
    "t_exam_question", metadata,
    Column("id", Integer, primary_key=True),
    Column("exam_id", Integer),
    Column("question_id", Integer),
    Column("score", Integer),
)

t_user_book = Table(
    "t_user_book", metadata,
    Column("id", Integer, primary_key=True),
    Column("user_id", Integer),
    Column("qu_id", Integer),
    Column("exam_id", Integer),
    Column("create_time", DateTime),
)

t_user_exams_score = Table(
    "t_user_exams_score", metadata,
    Column("id", Integer, primary_key=True),
    Column("user_id", Integer),
    Column("exam_id", Integer),
    Column("total_time", Integer),
    Column("user_time", Integer),
    Column("user_score", Integer),
    Column("limit_time", DateTime),
    Column("count", Integer),
    Column("state", Integer),            # 0考试中 1完成
    Column("create_time", DateTime),
    Column("whether_mark", Integer),     # -1无简答 0未阅 1已阅
)


# ===== 查询：智能出题 =====

def list_existing_question_contents(db: Session, repo_id: int, qu_type: int) -> list[str]:
    """读取题库中已有题目题干，用于去重。"""
    rows = db.execute(
        select(t_question.c.content).where(
            and_(t_question.c.repo_id == repo_id,
                 t_question.c.qu_type == qu_type,
                 t_question.c.is_deleted == 0)
        )
    ).scalars().all()
    return [r for r in rows if r]


def insert_question(db: Session, *, qu_type: int, content: str, analysis: Optional[str],
                    repo_id: int, user_id: int, image: Optional[str] = None) -> int:
    """插入一道题目，返回自增主键。"""
    result = db.execute(t_question.insert().values(
        qu_type=qu_type, image=image, content=content,
        create_time=datetime.now(), analysis=analysis,
        repo_id=repo_id, user_id=user_id, is_deleted=0,
    ))
    return result.inserted_primary_key[0]


def insert_option(db: Session, *, qu_id: int, is_right: int, content: str,
                   sort: int, image: Optional[str] = None) -> int:
    result = db.execute(t_option.insert().values(
        qu_id=qu_id, is_right=is_right, image=image,
        content=content, sort=sort, is_deleted=0,
    ))
    return result.inserted_primary_key[0]


# ===== 查询：主观题批改 =====

GRADING_SQL = text("""
SELECT q.id AS questionId,
       q.content AS questionContent,
       eq.score AS totalScore,
       o.content AS standardAnswer,
       eqa.answer_content AS userAnswer
FROM t_exam_qu_answer eqa
JOIN t_exam_question eq ON eqa.question_id = eq.question_id AND eq.exam_id = eqa.exam_id
JOIN t_question q ON eq.question_id = q.id AND q.is_deleted = 0
JOIN t_option o ON o.qu_id = q.id AND o.is_deleted = 0 AND o.is_right = 1
WHERE eqa.exam_id = :examId AND eqa.user_id = :userId AND q.qu_type = '4'
""")


def get_questions_for_grading(db: Session, exam_id: int, user_id: int) -> list[dict]:
    return [dict(r._mapping) for r in db.execute(GRADING_SQL, {"examId": exam_id, "userId": user_id})]


def update_grading_result(db: Session, *, exam_id: int, user_id: int,
                          question_id: int, ai_score: int, ai_reason: str) -> None:
    db.execute(
        update(t_exam_qu_answer).where(
            and_(t_exam_qu_answer.c.exam_id == exam_id,
                 t_exam_qu_answer.c.user_id == user_id,
                 t_exam_qu_answer.c.question_id == question_id)
        ).values(ai_score=ai_score, ai_reason=ai_reason)
    )


# ===== 查询：学情诊断 =====

WRONG_QUESTIONS_SQL = text("""
SELECT q.id AS questionId,
       q.content AS questionContent,
       q.qu_type AS quType,
       eqa.answer_content AS userAnswer,
       (SELECT o.content FROM t_option o
          WHERE o.qu_id = q.id AND o.is_right = 1 AND o.is_deleted = 0) AS correctAnswer,
       ub.exam_id AS examId,
       ub.create_time AS createTime
FROM t_user_book ub
JOIN t_question q ON ub.qu_id = q.id AND q.is_deleted = 0
LEFT JOIN t_exam_qu_answer eqa
       ON eqa.question_id = q.id AND eqa.user_id = ub.user_id AND eqa.exam_id = ub.exam_id
WHERE ub.user_id = :userId
ORDER BY ub.create_time DESC
""")


def list_wrong_questions(db: Session, user_id: int) -> list[dict]:
    return [dict(r._mapping) for r in db.execute(WRONG_QUESTIONS_SQL, {"userId": user_id})]


def list_exam_scores(db: Session, user_id: int) -> list[dict]:
    rows = db.execute(
        select(
            t_user_exams_score.c.id,
            t_user_exams_score.c.user_id,
            t_user_exams_score.c.exam_id,
            t_user_exams_score.c.user_score,
            t_user_exams_score.c.state,
            t_user_exams_score.c.create_time,
        ).where(
            and_(t_user_exams_score.c.user_id == user_id,
                 t_user_exams_score.c.state == 1)
        ).order_by(t_user_exams_score.c.create_time.desc())
    ).all()
    return [dict(r._mapping) for r in rows]


# ===== 查询：考试答疑 知识库加载 =====

def list_all_questions(db: Session) -> list[dict]:
    rows = db.execute(
        select(t_question.c.id, t_question.c.qu_type, t_question.c.content,
               t_question.c.analysis, t_question.c.repo_id)
        .where(t_question.c.is_deleted == 0)
    ).all()
    return [dict(r._mapping) for r in rows]


def list_all_options(db: Session) -> list[dict]:
    rows = db.execute(
        select(t_option.c.qu_id, t_option.c.content)
        .where(t_option.c.is_deleted == 0)
    ).all()
    return [dict(r._mapping) for r in rows]


def list_all_repos(db: Session) -> list[dict]:
    rows = db.execute(
        select(t_repo.c.id, t_repo.c.title).where(t_repo.c.is_deleted == 0)
    ).all()
    return [dict(r._mapping) for r in rows]
