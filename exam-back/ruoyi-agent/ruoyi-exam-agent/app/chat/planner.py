"""规划与执行（Plan-and-Execute）

为什么要这一层：
    只靠「预设流程」的助手，用户问一句没预设过的话就只能装傻。规划器做的事是
    先把用户的需求翻译成「系统里有没有能力做到」——用内置工具、系统接口，
    还是压根做不到；做得到就拆成步骤一步步跑，做不到就把原因讲清楚。

安全边界（很重要，别在重构时弄丢）：
    - 规划器**不判定权限**，它只读用户的角色 / 权限码来「避让」明显越权的动作；
    - 真正的拦截发生在 Java 侧：所有接口调用都带着用户自己的令牌走网关，
      网关与业务服务的 @SaCheckPermission 说了算。规划器猜错了也不会越权。
    - 写操作必须单独确认（见 flows.flow_general）。
"""

from __future__ import annotations

import json
import logging
from typing import Any

from pydantic import BaseModel, Field

from app.chat.models import TraceStep
from app.chat.runtime import call_llm, call_skill, call_tool
from app.skills.base import SkillContext
from app.tools.catalog import TOOLS

logger = logging.getLogger(__name__)

MAX_STEPS = 6
# 单步结果进上下文前先裁剪，避免一次拉回几百条把模型上下文撑爆
MAX_RESULT_CHARS = 2500


class PlanStep(BaseModel):
    """计划里的一步"""

    kind: str = Field(default="answer", description="tool / api / skill / answer")
    ref: str = ""
    title: str = ""
    params: dict[str, Any] = Field(default_factory=dict)
    risk: str = "read"


class Plan(BaseModel):
    """一次规划的结果"""

    goal: str = ""
    feasible: bool = True
    reason: str = ""
    steps: list[PlanStep] = Field(default_factory=list)
    direct_answer: str = ""

    def has_write(self) -> bool:
        return any(s.risk == "write" for s in self.steps)


def tool_catalog_text() -> str:
    """内置工具清单 -> 给模型看的文本"""
    lines = []
    for spec in TOOLS.values():
        params = ", ".join(f"{k}: {v}" for k, v in spec.params.items()) or "无"
        lines.append(f"- {spec.code}｜{spec.name}｜{spec.description}｜入参 {params}")
    return "\n".join(lines)


def parse_plan(raw: str) -> Plan | None:
    """解析模型输出的计划 JSON

    模型偶尔会在 JSON 外面裹一层 ```json，这里一并剥掉；
    解析不出来返回 None，由上层降级成「直接回答」。
    """
    if not raw:
        return None
    text = raw.strip()
    if text.startswith("```"):
        text = text.strip("`")
        # 去掉可能的语言标记
        text = text[text.find("\n") + 1:] if "\n" in text else text
        text = text.strip()
    start = text.find("{")
    end = text.rfind("}")
    if start < 0 or end <= start:
        return None
    try:
        data = json.loads(text[start: end + 1])
    except ValueError:
        logger.warning("计划 JSON 解析失败: %s", text[:200])
        return None
    if not isinstance(data, dict):
        return None
    steps: list[PlanStep] = []
    for item in (data.get("steps") or [])[:MAX_STEPS]:
        if not isinstance(item, dict):
            continue
        steps.append(
            PlanStep(
                kind=str(item.get("kind") or "answer"),
                ref=str(item.get("ref") or ""),
                title=str(item.get("title") or ""),
                params=item.get("params") if isinstance(item.get("params"), dict) else {},
                risk="write" if str(item.get("risk") or "read") == "write" else "read",
            )
        )
    return Plan(
        goal=str(data.get("goal") or ""),
        feasible=bool(data.get("feasible", True)),
        reason=str(data.get("reason") or ""),
        steps=steps,
        direct_answer=str(data.get("direct_answer") or ""),
    )


async def make_plan(
    question: str,
    ctx: SkillContext,
    trace: list[TraceStep],
    history: str = "",
) -> Plan | None:
    """让模型产出计划；顺带把系统接口清单喂给它"""
    manifest, step = await call_tool(
        "api_manifest", {}, tenant_id=ctx.tenant_id, title="读取系统能力清单"
    )
    trace.append(step)
    manifest_text = _manifest_text(manifest)

    content, step = await call_llm(
        "chat_plan",
        {
            "question": question,
            "history": history or "（无）",
            "identity": json.dumps(ctx.extra.get("identity") or {}, ensure_ascii=False),
            "tools": tool_catalog_text(),
            "manifest": manifest_text,
        },
        ctx,
        title="规划：判断可行性并拆解步骤",
        step_type="think",
    )
    trace.append(step)
    return parse_plan(content)


def _manifest_text(manifest: Any) -> str:
    if not isinstance(manifest, dict):
        return "（系统能力清单读取失败，只能使用内置工具）"
    items = manifest.get("items") or []
    lines = []
    for item in items:
        if not isinstance(item, dict):
            continue
        params = "；".join(item.get("params") or []) or "无"
        perm = item.get("permission") or "登录即可"
        lines.append(
            f"- {item.get('method')} {item.get('path')}｜{item.get('name')}"
            f"｜{item.get('desc')}｜入参 {params}｜需要权限 {perm}"
        )
    return "\n".join(lines) if lines else "（清单为空）"


def plan_md(plan: Plan) -> str:
    """计划 -> Markdown，用在确认卡与追问里"""
    lines = [f"**目标**：{plan.goal or '（未给出）'}", ""]
    if plan.steps:
        lines.append("| # | 动作 | 说明 | 类型 |")
        lines.append("| :-- | :-- | :-- | :-- |")
        for i, s in enumerate(plan.steps, 1):
            kind_label = {"tool": "内置工具", "api": "系统接口", "skill": "技能", "answer": "直接回答"}.get(s.kind, s.kind)
            lines.append(f"| {i} | {s.title or s.ref} | `{s.ref}` | {kind_label}{'（写操作）' if s.risk == 'write' else ''} |")
    return "\n".join(lines)


async def run_step(
    step: PlanStep, ctx: SkillContext, trace: list[TraceStep]
) -> tuple[Any, bool]:
    """执行一步，返回 (结果, 是否成功)"""
    if step.kind == "answer":
        trace.append(TraceStep(type="think", title=step.title or "直接回答", detail="无需调用任何能力", status="ok"))
        return None, True

    if step.kind == "tool":
        data, step_trace = await call_tool(
            step.ref, step.params, tenant_id=ctx.tenant_id, title=step.title or step.ref
        )
    elif step.kind == "skill":
        data, step_trace = await call_skill(step.ref, step.params, ctx, title=step.title or step.ref)
    elif step.kind == "api":
        payload = _api_payload(step, ctx)
        if payload is None:
            trace.append(
                TraceStep(type="tool", ref="api_call", status="error", title=step.title or step.ref,
                          detail=f"无法解析接口：{step.ref}")
            )
            return None, False
        data, step_trace = await call_tool(
            "api_call", payload, tenant_id=ctx.tenant_id, title=step.title or step.ref
        )
    else:
        trace.append(
            TraceStep(type="tool", status="error", title=step.title or step.ref, detail=f"未知步骤类型 {step.kind}")
        )
        return None, False

    trace.append(step_trace)
    return data, step_trace.status != "error"


def _api_payload(step: PlanStep, ctx: SkillContext) -> dict[str, Any] | None:
    """把「GET /exam/list」这种 ref 翻译成 api_call 的入参"""
    ref = (step.ref or "").strip()
    parts = ref.split(None, 1)
    if len(parts) != 2:
        return None
    method, path = parts[0].upper(), parts[1].strip()
    if not path.startswith("/"):
        return None
    params = dict(step.params or {})
    payload: dict[str, Any] = {
        "method": method,
        "path": path,
        # 身份三件套：Java 侧原样带到网关，权限由网关按这个用户判
        "token": ctx.extra.get("token") or "",
        "clientId": ctx.extra.get("client_id") or "",
        "userId": str(ctx.user_id or ""),
    }
    if isinstance(params.get("query"), dict):
        payload["query"] = params["query"]
    elif method == "GET":
        # 模型有时把 GET 参数直接写在 params 里，这里兜一下
        payload["query"] = {k: v for k, v in params.items() if k not in ("query", "body")}
    if isinstance(params.get("body"), dict):
        payload["body"] = params["body"]
    elif method != "GET":
        payload["body"] = {k: v for k, v in params.items() if k not in ("query", "body")}
    return payload


async def run_plan(
    plan: Plan, ctx: SkillContext, trace: list[TraceStep]
) -> list[dict[str, Any]]:
    """按序执行计划，收集每步结果"""
    results: list[dict[str, Any]] = []
    for index, step in enumerate(plan.steps, 1):
        data, ok = await run_step(step, ctx, trace)
        results.append(
            {
                "index": index,
                "title": step.title or step.ref,
                "ref": step.ref,
                "ok": ok,
                "data": _clip(data),
            }
        )
        if not ok and step.risk == "write":
            # 写操作失败就别往下走了，避免半套动作
            break
    return results


def _clip(data: Any) -> Any:
    """裁剪结果：列表只留前 20 条，字符串截断"""
    if data is None:
        return None
    if isinstance(data, list):
        return [_clip(item) for item in data[:20]]
    if isinstance(data, dict):
        clipped: dict[str, Any] = {}
        for key, value in data.items():
            if key in ("rows", "items", "records", "data", "list") and isinstance(value, list):
                clipped[key] = [_clip(v) for v in value[:20]]
            else:
                clipped[key] = _clip(value)
        return clipped
    if isinstance(data, str) and len(data) > MAX_RESULT_CHARS:
        return data[:MAX_RESULT_CHARS] + "…（已截断）"
    return data


def fallback_summary_md(question: str, plan: Plan, results: list[dict[str, Any]]) -> str:
    """模型没给出汇总时的兜底：至少把查到的数据摆出来，不留空气泡"""
    lines = [f"已按计划执行 **{len(results)}** 步，结果如下：", ""]
    for item in results:
        flag = "✅" if item.get("ok") else "❌"
        lines.append(f"### {flag} {item.get('index')}. {item.get('title')}")
        data = item.get("data")
        if data is None:
            lines.append("_没有返回数据_")
        else:
            text = json.dumps(data, ensure_ascii=False, indent=1)
            if len(text) > MAX_RESULT_CHARS:
                text = text[:MAX_RESULT_CHARS] + "\n…（已截断）"
            lines.append("```json\n" + text + "\n```")
        lines.append("")
    if not results:
        lines.append("_计划里没有可执行步骤_")
    return "\n".join(lines)
