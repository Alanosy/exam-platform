"""工具接口

把考试系统的业务能力暴露给 Agent，同时暴露给运维做连通性自检。

注意：Java 网关 ruoyi-exam-ai 目前尚未实现 /api/exam-tool/** 端点，
调用会返回 TOOL_ERROR。这是预期状态 —— 契约先定好，Java 侧实现后即打通。
"""

from __future__ import annotations

import logging
from typing import Any

from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.agent.guardrail import check_tool, guardrail_policy
from app.core.errors import AgentError, GuardrailBlockedError
from app.core.response import ok
from app.tools.base import ToolError, invoke_java
from app.tools.catalog import TOOLS, get_tool, list_tools

logger = logging.getLogger(__name__)
router = APIRouter()


class ToolInvokeRequest(BaseModel):
    tenant_id: str = "000000"
    payload: dict[str, Any] = Field(default_factory=dict)
    approved: bool = Field(default=False, description="写操作/高危操作必须显式批准")


@router.get("/tool/list")
async def tools() -> dict:
    return ok({"count": len(TOOLS), "items": list_tools(), "guardrail": guardrail_policy()})


@router.get("/tool/guardrail")
async def guardrail() -> dict:
    """安全护栏策略：哪些工具高危、步数上限、置信度门槛"""
    return ok(guardrail_policy())


@router.post("/tool/{code}/invoke")
async def invoke(code: str, req: ToolInvokeRequest) -> dict:
    spec = get_tool(code)
    if spec is None:
        raise AgentError(f"未注册的工具: {code}", code=404)

    # 护栏先于一切：影响考生命运的动作必须显式批准才放行
    try:
        check_tool(code, spec.risk_level, approved=req.approved)
    except GuardrailBlockedError as e:
        return ok({"ok": False, "code": code, "blocked": True, "error": str(e)})

    try:
        data = await invoke_java(spec, req.payload, tenant_id=req.tenant_id)
    except ToolError as e:
        # 工具不可达不算 500，是依赖未就绪，用业务码 502 + 明确提示
        return ok({"ok": False, "code": code, "error": str(e), "hint": "Java 网关 ruoyi-exam-ai 尚未实现该端点"})
    return ok({"ok": True, "code": code, "data": data})
