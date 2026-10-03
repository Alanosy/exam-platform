"""安全护栏

铁律（架构文档）：**凡是会影响考生命运的动作，一律不放进自主循环。**

影响半径分级：
    read-only    -> 可自主
    draft-only   -> 可自主（只写草稿态）
    affects-score     -> 必须人工审批
    affects-publish   -> 必须人工审批

护栏不是事后补丁，而是 Agent 执行每一步之前的一道闸门。
"""

from __future__ import annotations

import logging
from typing import Any

from app.core.errors import GuardrailBlockedError
from app.tools.base import WRITE

logger = logging.getLogger(__name__)

# 影响考生命运的工具：调用前必须显式带上 approved=True
HIGH_RISK_TOOLS = {
    "submit_mark_score",   # 写最终分
    "publish_exam",        # 发布考试
}

# 只写草稿态，可以自主
DRAFT_ONLY_TOOLS = {
    "save_question",
    "add_paper_questions",
    "save_ai_score",       # 写的是 ai_score，不覆盖人工分
}

MAX_STEPS = 12


def check_tool(tool_code: str, risk_level: str, approved: bool = False) -> None:
    """工具调用前的闸门"""
    if tool_code in HIGH_RISK_TOOLS:
        if not approved:
            raise GuardrailBlockedError(
                f"工具 {tool_code} 会影响考生成绩或考试可见性，"
                f"必须在请求中显式声明 approved=true 才能执行"
            )
        logger.warning("高危工具 %s 已被人工批准执行", tool_code)
        return

    if risk_level == WRITE and not approved:
        raise GuardrailBlockedError(f"写操作工具 {tool_code} 需要 approved=true")


def check_step(steps: int, limit: int | None = None) -> None:
    """步数闸门，防止 ReAct 循环失控烧 token"""
    cap = limit or MAX_STEPS
    if steps > cap:
        raise GuardrailBlockedError(f"执行步数超过上限 {cap}，已中止以防失控")


def needs_human_review(result: dict[str, Any]) -> bool:
    """判断一次 Skill 结果是否需要转人工"""
    if result.get("need_human"):
        return True
    confidence = result.get("confidence")
    if isinstance(confidence, (int, float)) and confidence < 0.6:
        return True
    if result.get("passed") is False:
        return True
    return False


def guardrail_policy() -> dict[str, Any]:
    """对外暴露护栏策略，供运维与前端展示"""
    return {
        "high_risk_tools": sorted(HIGH_RISK_TOOLS),
        "draft_only_tools": sorted(DRAFT_ONLY_TOOLS),
        "max_steps": MAX_STEPS,
        "confidence_floor": 0.6,
        "rule": "凡是会影响考生命运的动作，一律不放进自主循环，必须人工审批",
    }
