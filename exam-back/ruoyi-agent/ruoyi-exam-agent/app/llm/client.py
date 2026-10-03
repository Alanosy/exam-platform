"""LLM 客户端：OpenAI 兼容协议的极简实现

为什么不用 langchain：
- 本项目只需要 chat/completions 一个能力，且要精确控制超时、重试、主备链路；
- langchain 的版本组合（langchain / langchain-core / langchain-openai）极易打架，
  装一个 sentence-transformers 还会拉进整个 torch；
- 自己实现后依赖只有 httpx，安装快、行为可控。

支持任何 OpenAI 兼容协议的服务：DeepSeek / 通义（兼容模式）/ OpenAI /
Moonshot / 智谱 / 本地 vLLM & Ollama（/v1）。
"""

from __future__ import annotations

import logging
from typing import Any

import httpx

from app.config import settings
from app.gateway.models import ChatMessage, ChatRequest, ModelConfig

logger = logging.getLogger(__name__)

# 可重试的错误：限流、服务端错误、超时、连接失败
RETRIABLE_STATUS = {408, 409, 425, 429, 500, 502, 503, 504}


class LlmCallError(Exception):
    """LLM 调用失败"""

    def __init__(self, message: str, retriable: bool = True) -> None:
        super().__init__(message)
        self.retriable = retriable


def _mock_response(req: ChatRequest) -> dict[str, Any]:
    """MOCK 响应：只用于没有真实 Key 时验证链路，数据是空结构"""
    logger.warning("LLM MOCK 模式生效，返回空结构（仅供联调，严禁生产使用）")
    if not req.response_json:
        return {
            "choices": [
                {"message": {"role": "assistant", "content": "（MOCK 模式：未配置真实模型，返回占位内容）"}}
            ],
            "usage": {"prompt_tokens": 0, "completion_tokens": 0, "total_tokens": 0},
        }
    # 形状要跟提示词声明的一致，否则 object 型 Skill 拿到 [] 会解析失败
    content = "[]" if req.response_shape == "array" else "{}"
    return {
        "choices": [{"message": {"role": "assistant", "content": content}}],
        "usage": {"prompt_tokens": 0, "completion_tokens": 0, "total_tokens": 0},
    }


class LlmClient:
    """无状态客户端，一次调用一个 httpx 客户端"""

    async def chat(self, cfg: ModelConfig, req: ChatRequest) -> dict[str, Any]:
        """发起一次 chat/completions 调用，返回原始响应字典"""
        if settings.llm_mock:
            return _mock_response(req)

        if not cfg.base_url:
            raise LlmCallError(f"模型 {cfg.name} 未配置 api_base", retriable=False)
        if not cfg.api_key:
            raise LlmCallError(f"模型 {cfg.name} 未配置 api_key", retriable=False)

        url = f"{cfg.base_url}/chat/completions"
        payload: dict[str, Any] = {
            "model": cfg.model_name,
            "messages": [{"role": m.role, "content": m.content} for m in req.messages],
            "temperature": req.temperature if req.temperature is not None else cfg.temperature,
            "max_tokens": req.max_tokens if req.max_tokens is not None else cfg.max_tokens,
            "stream": False,
        }
        if req.response_json:
            # 部分国产模型不支持该字段，失败时由上层降级为普通模式重试
            payload["response_format"] = {"type": "json_object"}

        timeout = req.timeout or cfg.timeout or 60
        try:
            async with httpx.AsyncClient(timeout=timeout, trust_env=True) as client:
                resp = await client.post(
                    url,
                    json=payload,
                    headers={
                        "Authorization": f"Bearer {cfg.api_key}",
                        "Content-Type": "application/json",
                    },
                )
        except httpx.TimeoutException as e:
            raise LlmCallError(f"调用超时（{timeout}s）: {e}") from e
        except httpx.HTTPError as e:
            raise LlmCallError(f"网络错误: {e}") from e

        if resp.status_code != 200:
            body = resp.text[:300]
            retriable = resp.status_code in RETRIABLE_STATUS
            raise LlmCallError(f"HTTP {resp.status_code}: {body}", retriable=retriable)

        try:
            return resp.json()
        except ValueError as e:
            raise LlmCallError(f"响应不是合法 JSON: {resp.text[:200]}", retriable=False) from e


def extract_content(raw: dict[str, Any]) -> tuple[str, dict[str, int] | None]:
    """从 OpenAI 响应里取出文本与 token 用量"""
    choices = raw.get("choices") or []
    content = ""
    if choices:
        msg = choices[0].get("message") or {}
        content = msg.get("content") or ""
        # 少数模型（如推理型）把正文放在 reasoning_content
        if not content and msg.get("reasoning_content"):
            content = msg["reasoning_content"]
    usage = raw.get("usage") or None
    tokens: dict[str, int] | None = None
    if usage:
        tokens = {
            "prompt_tokens": int(usage.get("prompt_tokens") or 0),
            "completion_tokens": int(usage.get("completion_tokens") or 0),
            "total_tokens": int(usage.get("total_tokens") or 0),
        }
    return content or "", tokens


client = LlmClient()


def to_messages(system: str, user: str) -> list[ChatMessage]:
    msgs: list[ChatMessage] = []
    if system:
        msgs.append(ChatMessage(role="system", content=system))
    msgs.append(ChatMessage(role="user", content=user))
    return msgs
