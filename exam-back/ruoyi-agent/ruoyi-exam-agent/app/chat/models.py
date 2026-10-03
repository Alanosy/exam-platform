"""对话协议模型

前端聊天窗与 Agent 之间只认这几个结构：

- AskForm：Agent 需要补信息时的「中断卡」。kind=choice 时前端渲染选项列表，
  kind=form 时渲染表单，kind=confirm 时渲染确认按钮。
- TraceStep：一次运行流程里的一个节点（思考 / 调工具 / 跑技能 / 写库 / 结束）。
  前端按 type 渲染不同图标，按 status 渲染不同颜色。
- ChatResult：一回合的完整结果。reply 是 Markdown，前端直接渲染。

设计取舍：
    为什么中断不靠「多轮对话」而是显式 AskForm？
    因为纯自然语言追问会把「缺什么」藏在一大段话里，前端没法渲染成选项，
    用户还得再打一遍字。显式表单既省事又不会漏字段。
"""

from __future__ import annotations

from typing import Any

from pydantic import BaseModel, Field


class AskField(BaseModel):
    """中断表单的一个字段"""

    key: str
    label: str
    type: str = Field(default="text", description="text / number / select / multi / switch")
    options: list[dict[str, Any]] = Field(default_factory=list, description="[{label, value}]")
    value: Any = None
    required: bool = True
    placeholder: str = ""
    tip: str = ""


class AskForm(BaseModel):
    """需要用户补充信息时返回"""

    kind: str = Field(default="form", description="form / choice / confirm")
    title: str = ""
    desc: str = ""
    fields: list[AskField] = Field(default_factory=list)
    submit_text: str = "继续"
    cancel_text: str = "取消"


class TraceStep(BaseModel):
    """运行流程的一个节点"""

    id: str = ""
    type: str = Field(default="think", description="think / tool / skill / ask / write / done / error")
    title: str = ""
    detail: str = ""
    status: str = Field(default="ok", description="ok / error / waiting")
    ref: str = Field(default="", description="工具 code 或 Skill code")
    latency_ms: int = 0
    preview: list[str] = Field(default_factory=list, description="关键结果摘要，前端折叠展示")


class ChatResult(BaseModel):
    """一回合的结果"""

    session_id: str = ""
    reply: str = Field(default="", description="Markdown 正文")
    trace: list[TraceStep] = Field(default_factory=list)
    ask: AskForm | None = None
    intent: str = ""
    data: dict[str, Any] = Field(default_factory=dict)
