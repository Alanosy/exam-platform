"""Skill 抽象

为什么要有 Skill 这一层：
    直接写一堆 async def 调 LLM 也能跑，但会很快失控——提示词散落各处、
    无法单独测试、无法单独计量成本、换个模型就要全量回归。

Skill 的契约（对应架构文档 5.4）：
    1. 输入输出都是 Pydantic 模型，可独立单测
    2. 提示词外置到 prompts/<code>.yaml，改提示词不改代码
    3. 每次执行都记录 model / attempt_chain / latency / prompt_version
    4. 声明 risk_level，写操作类由 Guardrail 拦截
"""

from __future__ import annotations

import json
import logging
import time
import uuid
from abc import ABC, abstractmethod
from typing import Any, ClassVar

from pydantic import BaseModel, Field

from app.core.errors import SkillNotFoundError
from app.gateway.models import ChatMessage, ChatRequest
from app.gateway.router import router
from app.prompts.registry import registry as prompt_registry

logger = logging.getLogger(__name__)

READ = "READ"
WRITE = "WRITE"


class SkillContext(BaseModel):
    """执行上下文：租户、操作者、链路追踪、模型偏好"""

    tenant_id: str = "000000"
    user_id: str | None = None
    trace_id: str = Field(default_factory=lambda: uuid.uuid4().hex[:16])
    model_code: str | None = None
    extra: dict[str, Any] = Field(default_factory=dict)


class SkillResult(BaseModel):
    """统一的 Skill 输出外壳"""

    skill: str
    version: str = "v1"
    data: Any = None
    model: str = ""
    model_code: str = ""
    attempt_chain: str = ""
    latency_ms: int = 0
    prompt_version: str = ""
    need_human: bool = False
    raw: str | None = None
    trace_id: str = ""


class AiSkill(ABC):
    """Skill 基类

    子类只需实现：
    - 类变量：code / name / description / prompt_code / input_model
    - 方法：build_vars() 把入参映射成提示词变量
    - 方法：parse() 把 LLM 输出解析成结构化结果
    """

    code: ClassVar[str] = ""
    name: ClassVar[str] = ""
    description: ClassVar[str] = ""
    version: ClassVar[str] = "v1"
    prompt_code: ClassVar[str] = ""
    risk_level: ClassVar[str] = READ
    require_human_review: ClassVar[bool] = False

    @property
    @abstractmethod
    def input_model(self) -> type[BaseModel]:
        """入参模型"""

    @abstractmethod
    def build_vars(self, inp: BaseModel, ctx: SkillContext) -> dict[str, Any]:
        """入参 -> 提示词变量"""

    @abstractmethod
    def parse(self, content: str) -> Any:
        """LLM 输出 -> 结构化结果"""

    # ---------- 模板方法：子类一般不需要重写 ----------

    async def run(self, inp: BaseModel, ctx: SkillContext | None = None) -> SkillResult:
        ctx = ctx or SkillContext()
        tpl = prompt_registry.require(self.prompt_code or self.code)
        variables = self.build_vars(inp, ctx)
        system, user = tpl.render(**variables)

        params = tpl.params or {}
        messages = [ChatMessage(role="user", content=user)]
        if system:
            messages.insert(0, ChatMessage(role="system", content=system))

        req = ChatRequest(
            messages=messages,
            temperature=params.get("temperature"),
            max_tokens=params.get("max_tokens"),
            response_json=bool(params.get("response_json")),
            response_shape=str(params.get("response_shape", "auto")),
            model_code=ctx.model_code,
        )

        started = time.perf_counter()
        resp = await router.chat(
            req,
            tenant_id=ctx.tenant_id,
            biz_type="skill",
            skill_code=self.code,
        )
        elapsed = int((time.perf_counter() - started) * 1000)

        data = self.parse(resp.content)
        return SkillResult(
            skill=self.code,
            version=self.version,
            data=data,
            model=resp.model,
            model_code=resp.model_code,
            attempt_chain=resp.attempt_chain,
            latency_ms=elapsed,
            prompt_version=tpl.version,
            need_human=self.require_human_review,
            raw=resp.content,
            trace_id=ctx.trace_id,
        )

    def info(self) -> dict[str, Any]:
        tpl = prompt_registry.get(self.prompt_code or self.code)
        return {
            "code": self.code,
            "name": self.name,
            "version": self.version,
            "description": self.description,
            "risk_level": self.risk_level,
            "require_human_review": self.require_human_review,
            "prompt": tpl.public_dict() if tpl else None,
            "input_schema": self.input_model.model_json_schema(),
        }


def _json_or_default(value: Any, default: Any = None) -> Any:
    """把入参里的 dict/list 转成紧凑 JSON 字符串，供提示词使用"""
    if value is None or value == "" or value == [] or value == {}:
        return default if default is not None else "（无）"
    if isinstance(value, str):
        return value
    return json.dumps(value, ensure_ascii=False, indent=1)


def as_text(value: Any, default: str = "（无）") -> str:
    """任意入参转文本"""
    if value is None or value == "" or value == [] or value == {}:
        return default
    if isinstance(value, str):
        return value
    return json.dumps(value, ensure_ascii=False, indent=1)


def join_lines(value: Any, default: str = "（无）") -> str:
    """列表转可读的多行文本"""
    if not value:
        return default
    if isinstance(value, str):
        return value
    if isinstance(value, list):
        return "\n".join(f"- {v}" for v in value)
    return as_text(value, default)


def get_skill(code: str) -> AiSkill:
    from app.skills.registry import registry

    skill = registry.get(code)
    if skill is None:
        raise SkillNotFoundError(f"未注册的 Skill: {code}")
    return skill
