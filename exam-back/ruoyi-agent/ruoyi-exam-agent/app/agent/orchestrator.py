"""Orchestrator：Multi-Agent 调度

三种拓扑：
    pipeline  串行流水线（命题：出题 -> 审题 -> 校准）
    vote      并行投票（阅卷：严格评 + 宽松评，分歧大则仲裁）
    panel     并行专家组（试卷审查：多个审查员各查一块）

成本提醒：Multi-Agent 的 token 消耗是单 Agent 的 2-5 倍，
**只在「错误代价 > AI 成本」时用** —— 题目和分数值得，证书文案不值得。
"""

from __future__ import annotations

import asyncio
import logging
from typing import Any

from app.config import settings
from app.core.errors import SkillNotFoundError
from app.skills.base import SkillContext, SkillResult
from app.skills.registry import registry as skill_registry

logger = logging.getLogger(__name__)


async def _run_one(code: str, payload: dict[str, Any], ctx: SkillContext) -> SkillResult | None:
    skill = skill_registry.get(code)
    if skill is None:
        raise SkillNotFoundError(code)
    inp = skill.input_model(**payload)
    return await skill.run(inp, ctx)


async def pipeline(
    codes: list[str], payload: dict[str, Any], ctx: SkillContext
) -> dict[str, Any]:
    """串行流水线：上一步的输出作为下一步的输入之一"""
    shared = dict(payload)
    trace: list[dict[str, Any]] = []
    for code in codes:
        try:
            r = await _run_one(code, shared, ctx)
            if r is None:
                continue
            data = r.data.model_dump() if hasattr(r.data, "model_dump") else r.data
            if isinstance(data, dict):
                shared.update(data)
            else:
                shared["previous"] = data
            trace.append({"skill": code, "ok": True, "model": r.model, "latency_ms": r.latency_ms})
        except Exception as e:  # noqa: BLE001
            logger.warning("流水线环节 %s 失败: %s", code, e)
            trace.append({"skill": code, "ok": False, "error": str(e)})
            return {"ok": False, "failed_at": code, "trace": trace, "data": shared}
    return {"ok": True, "trace": trace, "data": shared}


async def vote(
    codes: list[str],
    payload: dict[str, Any],
    ctx: SkillContext,
    tolerance: float = 1.0,
) -> dict[str, Any]:
    """并行投票：多个 Agent 同时评同一份材料

    典型用法（阅卷双评）：
        vote(["mark_score", "mark_score"], payload)  # 不同 temperature 视作两个 Agent

    分歧超过 tolerance 时返回 need_arbitration=True，由上层转人工或仲裁。
    """
    tasks = [_run_one(c, payload, ctx) for c in codes]
    raw = await asyncio.gather(*tasks, return_exceptions=True)

    scores: list[float] = []
    details: list[dict[str, Any]] = []
    for code, r in zip(codes, raw):
        if isinstance(r, Exception):
            details.append({"skill": code, "ok": False, "error": str(r)})
            continue
        if r is None:
            continue
        data = r.data.model_dump() if hasattr(r.data, "model_dump") else dict(r.data)
        score = data.get("score") or data.get("adjusted_score") or 0
        scores.append(float(score))
        details.append({"skill": code, "ok": True, "score": score, "model": r.model, "data": data})

    if not scores:
        raise SkillNotFoundError("投票中所有 Agent 均失败")

    lo, hi = min(scores), max(scores)
    avg = round(sum(scores) / len(scores), 2)
    return {
        "ok": True,
        "scores": scores,
        "agreed_score": avg,
        "max_gap": round(hi - lo, 2),
        "need_arbitration": (hi - lo) > tolerance,
        "details": details,
    }


async def panel(
    codes: list[str], payload: dict[str, Any], ctx: SkillContext
) -> dict[str, Any]:
    """并行专家组：每个 Agent 独立输出一份意见，汇总成报告"""
    sem = asyncio.Semaphore(settings.agent_max_parallel)

    async def guarded(code: str) -> dict[str, Any]:
        async with sem:
            try:
                r = await _run_one(code, payload, ctx)
                if r is None:
                    return {"skill": code, "ok": False, "error": "无结果"}
                data = r.data.model_dump() if hasattr(r.data, "model_dump") else r.data
                return {"skill": code, "ok": True, "data": data, "model": r.model}
            except Exception as e:  # noqa: BLE001
                logger.warning("专家 %s 失败: %s", code, e)
                return {"skill": code, "ok": False, "error": str(e)}

    results = await asyncio.gather(*[guarded(c) for c in codes])
    return {
        "ok": all(r["ok"] for r in results),
        "opinions": list(results),
        "failed": [r["skill"] for r in results if not r["ok"]],
    }
