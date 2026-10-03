"""对话流程的执行原语：调工具 / 跑技能 / 问模型

每个原语都返回 (结果, TraceStep)，流程把 TraceStep 直接 append 到 trace 里，
这样「前端看到的步骤」和「真实执行过的动作」是同一份数据，不会对不上。

工具调用失败**不抛异常**，而是返回 status=error 的 TraceStep：
对话场景下一次工具失败不该让整轮对话崩掉，流程要能带着错误信息继续
（比如降级成「你自己选一个题库吧」）。
"""

from __future__ import annotations

import logging
import time
from typing import Any

from app.chat.models import TraceStep
from app.gateway.models import ChatMessage, ChatRequest
from app.gateway.router import router
from app.prompts.registry import registry as prompt_registry
from app.skills.base import SkillContext
from app.skills.registry import registry as skill_registry
from app.tools.catalog import get_tool
from app.tools.base import invoke_java

logger = logging.getLogger(__name__)


def _step_id() -> str:
    return f"s{int(time.time() * 1000) % 10_000_000}"


async def call_tool(
    code: str,
    payload: dict[str, Any],
    tenant_id: str = "000000",
    title: str = "",
) -> tuple[Any, TraceStep]:
    """调用一个 Java 侧工具"""
    spec = get_tool(code)
    step = TraceStep(id=_step_id(), type="tool", ref=code, status="ok")
    if spec is None:
        step.status = "error"
        step.title = f"调用工具 {code}"
        step.detail = "工具未注册"
        return None, step

    step.title = title or f"调用工具 · {spec.name}"
    step.detail = f"`{spec.method} {spec.path}`"

    started = time.perf_counter()
    try:
        data = await invoke_java(spec, payload, tenant_id=tenant_id)
    except Exception as e:  # noqa: BLE001
        step.status = "error"
        step.detail = f"{step.detail}\n失败：{e}"
        logger.warning("对话工具 %s 调用失败: %s", code, e)
        return None, step
    finally:
        step.latency_ms = int((time.perf_counter() - started) * 1000)
    return data, step


async def call_skill(
    code: str,
    payload: dict[str, Any],
    ctx: SkillContext,
    title: str = "",
) -> tuple[Any, TraceStep]:
    """跑一个 Skill"""
    skill = skill_registry.get(code)
    step = TraceStep(id=_step_id(), type="skill", ref=code, status="ok")
    if skill is None:
        step.status = "error"
        step.title = f"执行技能 {code}"
        step.detail = "技能未注册"
        return None, step

    step.title = title or f"执行技能 · {skill.name}"
    step.detail = skill.description

    started = time.perf_counter()
    try:
        result = await skill.run(skill.input_model(**payload), ctx)
    except Exception as e:  # noqa: BLE001
        step.status = "error"
        step.detail = str(e)
        logger.warning("对话技能 %s 执行失败: %s", code, e)
        return None, step
    finally:
        step.latency_ms = int((time.perf_counter() - started) * 1000)

    step.detail = f"{skill.name} · 模型 {result.model_code or '-'} / 提示词 {result.prompt_version}"
    data = result.data
    if hasattr(data, "model_dump"):
        data = data.model_dump()
    elif isinstance(data, list):
        data = [d.model_dump() if hasattr(d, "model_dump") else d for d in data]
    return data, step


async def call_llm(
    prompt_code: str,
    variables: dict[str, Any],
    ctx: SkillContext,
    title: str = "模型分析",
    step_type: str = "skill",
) -> tuple[str, TraceStep]:
    """按提示词模板直接调一次模型，返回纯文本

    与 Skill 的区别：这里要的是**一段 Markdown 正文**（给聊天窗渲染），
    不是结构化对象，所以不走 parse，也不注册成 Skill。
    """
    step = TraceStep(id=_step_id(), type=step_type, ref=prompt_code, status="ok", title=title)
    tpl = prompt_registry.get(prompt_code)
    if tpl is None:
        step.status = "error"
        step.detail = f"提示词 {prompt_code} 未注册"
        return "", step

    system, user = tpl.render(**variables)
    params = tpl.params or {}
    messages = []
    if system:
        messages.append(ChatMessage(role="system", content=system))
    messages.append(ChatMessage(role="user", content=user))

    req = ChatRequest(
        messages=messages,
        temperature=params.get("temperature"),
        max_tokens=params.get("max_tokens"),
        response_json=bool(params.get("response_json")),
        response_shape=str(params.get("response_shape", "auto")),
        model_code=ctx.model_code,
    )
    step.detail = f"提示词 {tpl.name} · {tpl.version}"

    started = time.perf_counter()
    try:
        resp = await router.chat(req, tenant_id=ctx.tenant_id, biz_type="chat", skill_code=prompt_code)
    except Exception as e:  # noqa: BLE001
        step.status = "error"
        step.detail = str(e)
        logger.warning("对话模型调用失败 %s: %s", prompt_code, e)
        return "", step
    finally:
        step.latency_ms = int((time.perf_counter() - started) * 1000)

    step.detail = f"{step.detail} · 模型 {resp.model_code or '-'}"
    return resp.content or "", step
