"""模型网关路由器：主备切换 + 熔断 + 重试 + 审计

调用流程（架构文档 6.4）：
    1. 取候选链（priority 升序，主用在前）
    2. 过滤掉处于 OPEN 熔断态的
    3. 取第一个发起调用，失败则在 retry_count 内重试同一模型
    4. 仍失败 -> 记 DEGRADED、切下一个候选 -> 回到 3
    5. 全部候选失败 -> 抛 AiUnavailableError（**绝不静默降级为假成功**）
    6. 成功 -> 写调用流水

「绝不静默降级」是硬约束：AI 阅卷若在主模型挂掉后静默返回 0 分，
老师会以为学生真的得了 0 分 —— 这比报错危险得多。
"""

from __future__ import annotations

import logging
import time
from typing import Any

from app.config import settings
from app.core.errors import AiUnavailableError, ModelConfigError
from app.core.logging import CallRecord, call_log
from app.gateway.circuit import CircuitBreaker
from app.gateway.models import ChatRequest, ChatResponse, ModelConfig
from app.gateway.providers import registry
from app.llm.client import LlmCallError, client, extract_content

logger = logging.getLogger(__name__)


class AiModelRouter:
    def __init__(self) -> None:
        self.circuit = CircuitBreaker(
            fail_threshold=settings.model_fuse_threshold,
            cooldown=settings.model_fuse_cooldown,
        )

    async def _candidates(
        self, tenant_id: str, model_code: str | None, force: bool = False
    ) -> list[ModelConfig]:
        cfgs = await registry.list_models(tenant_id, force=force)
        if not cfgs:
            raise ModelConfigError(
                "没有可用的模型配置：请在后台「AI 模型配置」中添加并启用，"
                "或设置 AGENT_LLM_MODELS / AGENT_LLM_API_KEY 环境变量"
            )
        if model_code:
            # 调试用：强制指定，仍走熔断但不走主备链
            hit = [c for c in cfgs if c.code == model_code or c.name == model_code]
            if not hit:
                raise ModelConfigError(f"未找到模型 {model_code}")
            return hit
        if not settings.model_failover_enabled:
            return cfgs[:1]
        return cfgs

    async def chat(
        self,
        req: ChatRequest,
        tenant_id: str = "000000",
        biz_type: str = "chat",
        skill_code: str = "",
        force_reload: bool = False,
    ) -> ChatResponse:
        """发起一次带主备切换的调用"""
        candidates = await self._candidates(tenant_id, req.model_code, force=force_reload)

        attempt_chain: list[str] = []
        last_error = ""
        started = time.perf_counter()

        for cfg in candidates:
            if not self.circuit.allow(cfg.code):
                logger.info("模型 %s 处于熔断态，跳过", cfg.name)
                continue

            attempt_chain.append(cfg.name)
            retries = max(0, min(cfg.retry_count, 3)) if settings.model_failover_enabled else 0

            for attempt in range(retries + 1):
                try:
                    raw = await client.chat(cfg, req)
                    content, tokens = extract_content(raw)
                    self.circuit.record_success(cfg.code)
                    latency = int((time.perf_counter() - started) * 1000)

                    if settings.call_log_enabled:
                        call_log.append(
                            CallRecord(
                                ts=time.time(),
                                biz_type=biz_type,
                                skill_code=skill_code,
                                model=cfg.model_name,
                                attempt_chain=">".join(attempt_chain),
                                success=True,
                                latency_ms=latency,
                                prompt_tokens=(tokens or {}).get("prompt_tokens"),
                                completion_tokens=(tokens or {}).get("completion_tokens"),
                            )
                        )

                    return ChatResponse(
                        content=content,
                        model=cfg.model_name,
                        model_code=cfg.code,
                        attempt_chain=">".join(attempt_chain),
                        attempts=len(attempt_chain),
                        latency_ms=latency,
                        prompt_tokens=(tokens or {}).get("prompt_tokens"),
                        completion_tokens=(tokens or {}).get("completion_tokens"),
                        total_tokens=(tokens or {}).get("total_tokens"),
                    )
                except LlmCallError as e:
                    last_error = str(e)
                    if not e.retriable:
                        # 参数类错误（401/404/未配置）重试也没用，直接切下一个
                        self.circuit.record_failure(cfg.code, last_error)
                        break
                    if attempt < retries:
                        logger.warning(
                            "模型 %s 第 %d 次调用失败，重试中: %s", cfg.name, attempt + 1, last_error
                        )
                        continue
                    self.circuit.record_failure(cfg.code, last_error)
                    logger.warning("模型 %s 调用失败，切换到备用: %s", cfg.name, last_error)

        if settings.call_log_enabled:
            call_log.append(
                CallRecord(
                    ts=time.time(),
                    biz_type=biz_type,
                    skill_code=skill_code,
                    model="",
                    attempt_chain=">".join(attempt_chain) or "(无可用候选)",
                    success=False,
                    latency_ms=int((time.perf_counter() - started) * 1000),
                    error=last_error[:300],
                )
            )

        raise AiUnavailableError(
            f"所有候选模型均调用失败（尝试链: {'>'.join(attempt_chain) or '无'}）: {last_error}"
        )

    async def list_models(self, tenant_id: str = "000000", force: bool = False) -> list[ModelConfig]:
        return await registry.list_models(tenant_id, force=force)

    def health(self, tenant_id: str = "000000") -> dict[str, Any]:
        """网关健康快照，供 /health 与运维接口使用"""
        return {
            "failover_enabled": settings.model_failover_enabled,
            "config_source": settings.model_source,
            "circuit": self.circuit.all_snapshot(),
            "provider": registry.source_status(),
            "call": call_log.stats(),
        }


router = AiModelRouter()
