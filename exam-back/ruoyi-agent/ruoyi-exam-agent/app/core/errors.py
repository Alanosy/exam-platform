"""异常定义

设计原则（对齐架构文档）：
- **全部模型都失败时必须抛异常，绝不静默降级为假成功。**
  AI 阅卷若静默返回 0 分或满分，比直接报错危险得多。
"""

from __future__ import annotations

from typing import Any


class AgentError(Exception):
    """所有业务异常的基类"""

    code: int = 500

    def __init__(self, message: str, code: int | None = None, data: Any = None) -> None:
        super().__init__(message)
        self.message = message
        if code is not None:
            self.code = code
        self.data = data


class AiUnavailableError(AgentError):
    """所有候选模型均不可用（主备链全部失败）

    这是最关键的异常：上层必须感知，不能吞掉。
    """

    code = 503


class ModelConfigError(AgentError):
    """模型配置缺失或非法（如一条启用配置都没有）"""

    code = 500


class SkillNotFoundError(AgentError):
    """Skill 未注册"""

    code = 404


class SkillInputError(AgentError):
    """Skill 入参校验失败"""

    code = 400


class ToolError(AgentError):
    """工具调用失败（Java 网关不可达 / 返回非 200）"""

    code = 502


class LlmOutputError(AgentError):
    """LLM 输出无法解析为期望结构"""

    code = 502


class GuardrailBlockedError(AgentError):
    """被安全护栏拦截（需要人工审批 / 命中敏感动作）"""

    code = 403
