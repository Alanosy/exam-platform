"""工具层基础设施

工具 = 对 Java 侧业务能力的 REST 封装。

现状：ruoyi-exam-ai（Java 网关）尚未实现这些 REST 端点，
所以调用会返回 TOOL_ERROR。这是**预期行为**——工具层先把契约定好，
Java 侧按契约实现后即可直接打通，Agent 代码不用改。

约定：
- 路径前缀 /api/exam-tool/**
- 鉴权：可选 Bearer token（AGENT_JAVA_GATEWAY_TOKEN）
- 写操作工具必须 risk_level=WRITE，由 Guardrail 拦截
"""

from __future__ import annotations

import logging
from typing import Any

import httpx
from pydantic import BaseModel, Field

from app.config import settings
from app.core.errors import ToolError

logger = logging.getLogger(__name__)

READ = "READ"
WRITE = "WRITE"


class ToolSpec(BaseModel):
    """工具声明"""

    code: str
    name: str
    description: str
    path: str
    method: str = "POST"
    risk_level: str = READ
    params: dict[str, Any] = Field(default_factory=dict, description="JSON Schema 风格的参数说明")

    def info(self) -> dict[str, Any]:
        return {
            "code": self.code,
            "name": self.name,
            "description": self.description,
            "risk_level": self.risk_level,
            "endpoint": f"{self.method} {self.path}",
            "params": self.params,
        }


class ToolResult(BaseModel):
    ok: bool = True
    code: str = ""
    data: Any = None
    message: str = ""
    latency_ms: int = 0


async def invoke_java(spec: ToolSpec, payload: dict[str, Any], tenant_id: str = "000000") -> Any:
    """调用 Java 网关暴露的工具端点"""
    if not settings.tools_enabled:
        raise ToolError("工具层已关闭（AGENT_TOOLS_ENABLED=false）")

    url = f"{settings.java_gateway_base_url.rstrip('/')}{spec.path}"
    headers = {"Content-Type": "application/json", "X-Tenant-Id": tenant_id}
    if settings.java_gateway_token:
        headers["Authorization"] = f"Bearer {settings.java_gateway_token}"

    try:
        async with httpx.AsyncClient(timeout=settings.java_gateway_timeout) as client:
            resp = await client.request(spec.method, url, json=payload, headers=headers)
    except httpx.HTTPError as e:
        raise ToolError(f"Java 网关不可达: {e}") from e

    if resp.status_code != 200:
        raise ToolError(f"工具 {spec.code} 调用失败 HTTP {resp.status_code}: {resp.text[:200]}")

    try:
        body = resp.json()
    except ValueError as e:
        raise ToolError(f"工具 {spec.code} 返回非 JSON: {resp.text[:200]}") from e

    # 兼容 R<T> 包装
    if isinstance(body, dict) and "code" in body:
        if body.get("code") != 200:
            raise ToolError(f"工具 {spec.code} 业务失败: {body.get('msg')}")
        return body.get("data")
    return body
