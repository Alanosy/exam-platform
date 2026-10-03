"""模型配置数据模型

字段与 ry-cloud.ai_model_config 表一一对应，
保证从 MySQL 直读时能原样映射，不需要额外的转换层。

表结构（只读，禁止写入）：
    id, tenant_id, config_name, model_type, model_name, api_base, api_key,
    temperature, max_tokens, timeout, retry_count, priority, weight,
    status, remark, del_flag
"""

from __future__ import annotations

from typing import Any

from pydantic import BaseModel, Field


class ModelConfig(BaseModel):
    """一条启用的模型配置"""

    code: str = Field(..., description="唯一标识，MySQL 源用 id，env 源用 config_name")
    name: str = Field(default="", description="配置名称，展示用")
    model_name: str = Field(..., description="实际模型标识，如 deepseek-chat")
    base_url: str = Field(default="", description="API 地址，OpenAI 兼容协议")
    api_key: str = Field(default="", description="密钥，日志中必须脱敏")
    temperature: float = Field(default=0.2)
    max_tokens: int = Field(default=2048)
    timeout: int = Field(default=60, description="单次调用超时（秒）")
    retry_count: int = Field(default=1, description="单模型内重试次数")
    priority: int = Field(default=10, description="越小越优先，主用取最小值")
    weight: int = Field(default=100, description="预留给加权分流，主备模式不使用")
    tenant_id: str = Field(default="000000")
    source: str = Field(default="env", description="配置来源 mysql / env / mock")
    extra: dict[str, Any] = Field(default_factory=dict)

    @property
    def masked_key(self) -> str:
        """脱敏后的密钥，用于日志与接口展示"""
        k = self.api_key or ""
        if len(k) <= 8:
            return "***"
        return f"{k[:6]}***{k[-4:]}"

    def public_dict(self) -> dict[str, Any]:
        """对外展示用（不含 api_key 明文）"""
        d = self.model_dump(exclude={"api_key"})
        d["api_key_masked"] = self.masked_key
        return d


class ChatMessage(BaseModel):
    role: str = Field(..., description="system / user / assistant")
    content: str


class ChatRequest(BaseModel):
    """统一的聊天请求"""

    messages: list[ChatMessage]
    temperature: float | None = None
    max_tokens: int | None = None
    timeout: int | None = None
    response_json: bool = Field(default=False, description="是否要求返回 JSON（走 json_object 模式）")
    response_shape: str = Field(
        default="auto",
        description="期望的 JSON 形状：array / object / auto。来自提示词 params.response_shape",
    )
    model_code: str | None = Field(default=None, description="强制指定模型，跳过主备链（调试用）")


class ChatResponse(BaseModel):
    """统一的聊天响应"""

    content: str
    model: str = Field(default="", description="实际使用的模型名")
    model_code: str = Field(default="", description="实际使用的配置编码")
    attempt_chain: str = Field(default="", description="尝试链，如 DeepSeek>Qwen")
    attempts: int = Field(default=1)
    latency_ms: int = 0
    prompt_tokens: int | None = None
    completion_tokens: int | None = None
    total_tokens: int | None = None
