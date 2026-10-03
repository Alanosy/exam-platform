"""LLM 门面（兼容层）

历史版本在这里直接调 langchain；重构后 LLM 能力已下沉到：
    app/llm/client.py       OpenAI 兼容协议客户端
    app/gateway/router.py   主备切换 / 熔断 / 审计
    app/skills/**           各业务 Skill

本模块保留 app.services.llm 的导入路径以兼容旧代码，
**新代码请直接用 app.gateway.router.router 或 Skill，不要再走这里。**
"""

from __future__ import annotations

from app.gateway.models import ChatRequest
from app.gateway.router import router as _router


class LlmService:
    """极薄包装，等价于直接调 AiModelRouter"""

    async def chat(self, messages: list[dict], **kwargs) -> str:
        req = ChatRequest(messages=messages, **kwargs)  # type: ignore[arg-type]
        resp = await _router.chat(req)
        return resp.content


def get_llm() -> LlmService:
    return LlmService()


__all__ = ["LlmService", "get_llm"]
