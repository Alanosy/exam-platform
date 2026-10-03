"""Executor：按计划逐步执行（Plan-and-Execute + ReAct 风格的重规划）

执行语义：
- kind=skill：调 Skill
- kind=tool：调 Java 网关工具（写操作需 approved=true）
- 任一步失败：默认中止并回传已完成的部分结果（长任务不能白跑）
- 每步结果记入 trace，便于排查「卡在哪一步」

ReAct 的味道体现在：每步执行完会把结果塞回上下文，
后续步骤可以引用前序结果（payload_from）。
"""

from __future__ import annotations

import logging
import time
from dataclasses import dataclass, field
from typing import Any

from app.agent.guardrail import MAX_STEPS, check_step, check_tool
from app.agent.planner import Plan, Step, get_plan
from app.core.errors import AgentError, SkillNotFoundError
from app.skills.base import SkillContext, SkillResult
from app.skills.registry import registry as skill_registry
from app.tools.base import ToolError
from app.tools.catalog import get_tool
from app.tools.base import invoke_java

logger = logging.getLogger(__name__)


@dataclass
class StepResult:
    id: str
    name: str
    kind: str
    ref: str
    ok: bool
    latency_ms: int = 0
    data: Any = None
    error: str = ""
    need_human: bool = False


@dataclass
class TaskResult:
    task_type: str
    ok: bool
    steps: list[StepResult] = field(default_factory=list)
    data: dict[str, Any] = field(default_factory=dict)
    error: str = ""
    latency_ms: int = 0
    need_human: bool = False


async def _run_skill(step: Step, payload: dict[str, Any], ctx: SkillContext) -> SkillResult:
    skill = skill_registry.get(step.ref)
    if skill is None:
        raise SkillNotFoundError(f"计划引用了未注册的 Skill: {step.ref}")
    model_input = skill.input_model
    try:
        inp = model_input(**payload)
    except Exception as e:  # noqa: BLE001
        raise AgentError(f"Skill {step.ref} 入参不合法: {e}") from e
    return await skill.run(inp, ctx)


async def _run_tool(
    step: Step, payload: dict[str, Any], ctx: SkillContext, approved: bool
) -> Any:
    spec = get_tool(step.ref)
    if spec is None:
        raise AgentError(f"计划引用了未注册的工具: {step.ref}")
    check_tool(step.ref, spec.risk_level, approved=approved)
    return await invoke_java(spec, payload, tenant_id=ctx.tenant_id)


async def execute_plan(
    task_type: str,
    params: dict[str, Any],
    ctx: SkillContext,
    approved: bool = False,
    max_steps: int | None = None,
    stop_on_error: bool = True,
) -> TaskResult:
    """执行一个预置计划

    :param approved: 是否批准写操作（高危工具必须显式批准）
    :param stop_on_error: 失败是否中止；False 时跳过失败步骤继续跑
    """
    plan: Plan | None = get_plan(task_type)
    if plan is None:
        return TaskResult(task_type=task_type, ok=False, error=f"未知的 task_type: {task_type}")

    started = time.perf_counter()
    results: list[StepResult] = []
    shared: dict[str, Any] = dict(params)
    need_human = False

    for i, step in enumerate(plan.steps):
        check_step(i + 1, max_steps or MAX_STEPS)
        step_start = time.perf_counter()

        # 入参：优先用 params 里针对该步骤的覆盖，其次用整体 params
        payload = dict(params.get(step.id) or shared)

        try:
            if step.kind == "skill":
                r = await _run_skill(step, payload, ctx)
                data = r.data.model_dump() if hasattr(r.data, "model_dump") else r.data
                if isinstance(data, list):
                    data = [d.model_dump() if hasattr(d, "model_dump") else d for d in data]
                if r.need_human:
                    need_human = True
                if isinstance(data, dict) and data.get("need_human"):
                    need_human = True
            elif step.kind == "tool":
                data = await _run_tool(step, payload, ctx, approved)
            else:
                raise AgentError(f"未知的步骤类型: {step.kind}")

            # 结果回灌上下文，供后续步骤引用（ReAct 的 Observation）
            shared[step.id] = data
            if isinstance(data, dict):
                shared.update({f"{step.id}.{k}": v for k, v in data.items()})

            results.append(
                StepResult(
                    id=step.id,
                    name=step.name,
                    kind=step.kind,
                    ref=step.ref,
                    ok=True,
                    latency_ms=int((time.perf_counter() - step_start) * 1000),
                    data=data,
                )
            )
        except (ToolError, AgentError) as e:
            logger.warning("步骤 %s(%s) 失败: %s", step.id, step.ref, e)
            results.append(
                StepResult(
                    id=step.id,
                    name=step.name,
                    kind=step.kind,
                    ref=step.ref,
                    ok=False,
                    latency_ms=int((time.perf_counter() - step_start) * 1000),
                    error=str(e),
                )
            )
            if stop_on_error:
                return TaskResult(
                    task_type=task_type,
                    ok=False,
                    steps=results,
                    data={s.id: s.data for s in results if s.ok},
                    error=str(e),
                    latency_ms=int((time.perf_counter() - started) * 1000),
                    need_human=need_human,
                )
        except Exception as e:  # noqa: BLE001
            logger.exception("步骤 %s 执行异常: %s", step.id, e)
            results.append(
                StepResult(
                    id=step.id, name=step.name, kind=step.kind, ref=step.ref,
                    ok=False, error=str(e),
                )
            )
            if stop_on_error:
                return TaskResult(
                    task_type=task_type, ok=False, steps=results,
                    data={s.id: s.data for s in results if s.ok},
                    error=str(e),
                    latency_ms=int((time.perf_counter() - started) * 1000),
                    need_human=need_human,
                )

    ok = all(s.ok for s in results)
    return TaskResult(
        task_type=task_type,
        ok=ok,
        steps=results,
        data={s.id: s.data for s in results if s.ok},
        latency_ms=int((time.perf_counter() - started) * 1000),
        need_human=need_human,
    )
