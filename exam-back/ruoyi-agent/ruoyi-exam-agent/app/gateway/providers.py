"""模型配置源

设计要点：
1. **MySQL 只读**：只 SELECT ry-cloud.ai_model_config，绝不写入。
   排序规则与 Java 侧保持一致 —— 取启用（status='0'）且未删（del_flag='0'）的，
   按 priority 升序取第一条为主用，其余依次作备用。
2. **env 兜底**：MySQL 没配、连不上或查不到数据时，回落到环境变量。
   本地开发、Docker 单机、以及客户还没在后台配模型时都能跑。
3. **任一源失败不影响启动**：异常只记日志并返回空列表，由 Registry 继续尝试下一个源。
"""

from __future__ import annotations

import asyncio
import json
import logging
import time
from typing import Any

from app.config import settings
from app.gateway.models import ModelConfig

logger = logging.getLogger(__name__)


class ModelProvider:
    """配置源基类"""

    name: str = "base"

    async def load(self, tenant_id: str = "000000") -> list[ModelConfig]:
        raise NotImplementedError


class MysqlModelProvider(ModelProvider):
    """从 ry-cloud.ai_model_config 读取（只读）"""

    name = "mysql"

    def __init__(self) -> None:
        self._last_error: str | None = None

    def _query(self, tenant_id: str) -> list[dict[str, Any]]:
        import pymysql  # 局部导入：没装 pymysql 也能以 env 源运行

        conn = pymysql.connect(
            host=settings.mysql_host,
            port=settings.mysql_port,
            user=settings.mysql_user,
            password=settings.mysql_password,
            database=settings.mysql_database,
            connect_timeout=3,
            charset="utf8mb4",
        )
        try:
            with conn.cursor() as cur:
                # 只查启用且未删的；priority 小者优先，id 兜底保证顺序稳定
                cur.execute(
                    """
                    SELECT id, config_name, model_type, model_name, api_base, api_key,
                           temperature, max_tokens, timeout, retry_count, priority, weight, tenant_id
                    FROM ai_model_config
                    WHERE del_flag = '0' AND status = '0' AND tenant_id = %s
                    ORDER BY priority ASC, id ASC
                    """,
                    (tenant_id,),
                )
                cols = [d[0] for d in cur.description]
                return [dict(zip(cols, row)) for row in cur.fetchall()]
        finally:
            conn.close()

    async def load(self, tenant_id: str = "000000") -> list[ModelConfig]:
        if not settings.mysql_enabled:
            return []
        try:
            rows = await asyncio.to_thread(self._query, tenant_id)
        except Exception as e:  # noqa: BLE001
            self._last_error = str(e)
            logger.warning("MySQL 模型配置源不可用，降级到下一源: %s", e)
            return []

        if not rows:
            logger.info("MySQL 未读到启用中的模型配置（tenant=%s），降级到下一源", tenant_id)
            return []

        out: list[ModelConfig] = []
        for r in rows:
            if not (r.get("api_key") and r.get("model_name")):
                logger.warning("模型配置 %s 缺少 api_key/model_name，跳过", r.get("config_name"))
                continue
            out.append(
                ModelConfig(
                    code=str(r["id"]),
                    name=r.get("config_name") or r.get("model_name") or "",
                    model_name=r["model_name"],
                    base_url=(r.get("api_base") or "").rstrip("/"),
                    api_key=r["api_key"],
                    temperature=float(r.get("temperature") or 0.2),
                    max_tokens=int(r.get("max_tokens") or 2048),
                    timeout=int((r.get("timeout") or 60000) / 1000) or settings.model_default_timeout,
                    retry_count=int(r.get("retry_count") or 1),
                    priority=int(r.get("priority") or 10),
                    weight=int(r.get("weight") or 100),
                    tenant_id=str(r.get("tenant_id") or "000000"),
                    source="mysql",
                    extra={"model_type": r.get("model_type")},
                )
            )
        logger.info("MySQL 载入 %d 条模型配置: %s", len(out), [m.name for m in out])
        return out

    @property
    def last_error(self) -> str | None:
        return self._last_error


class EnvModelProvider(ModelProvider):
    """从环境变量读取

    单模型沿用 AGENT_LLM_*；多模型用 AGENT_LLM_MODELS（JSON 数组）：
        [{"name":"DeepSeek","model_name":"deepseek-chat",
          "base_url":"https://api.deepseek.com/v1","api_key":"sk-xxx","priority":10}]
    """

    name = "env"

    async def load(self, tenant_id: str = "000000") -> list[ModelConfig]:
        raw = getattr(settings, "llm_models", "") or ""
        if raw:
            try:
                items = json.loads(raw) if isinstance(raw, str) else raw
                out: list[ModelConfig] = []
                for i, it in enumerate(items):
                    if not it.get("api_key") or not it.get("model_name"):
                        continue
                    out.append(
                        ModelConfig(
                            code=it.get("name") or f"env-{i}",
                            name=it.get("name") or it["model_name"],
                            model_name=it["model_name"],
                            base_url=(it.get("base_url") or "").rstrip("/"),
                            api_key=it["api_key"],
                            temperature=float(it.get("temperature", settings.llm_temperature)),
                            max_tokens=int(it.get("max_tokens", settings.llm_max_tokens)),
                            timeout=int(it.get("timeout", settings.llm_timeout)),
                            retry_count=int(it.get("retry_count", settings.model_max_retry)),
                            priority=int(it.get("priority", 10 + i)),
                            tenant_id=tenant_id,
                            source="env",
                        )
                    )
                if out:
                    logger.info("env 载入 %d 条模型配置: %s", len(out), [m.name for m in out])
                    return out
            except Exception as e:  # noqa: BLE001
                logger.warning("AGENT_LLM_MODELS 解析失败，回落到单模型环境变量: %s", e)

        if settings.llm_api_key and settings.llm_model:
            return [
                ModelConfig(
                    code="env-default",
                    name=settings.llm_model,
                    model_name=settings.llm_model,
                    base_url=(settings.llm_base_url or "").rstrip("/"),
                    api_key=settings.llm_api_key,
                    temperature=settings.llm_temperature,
                    max_tokens=settings.llm_max_tokens,
                    timeout=settings.llm_timeout,
                    retry_count=settings.model_max_retry,
                    priority=10,
                    tenant_id=tenant_id,
                    source="env",
                )
            ]
        return []


class MockModelProvider(ModelProvider):
    """MOCK 兜底源：AGENT_LLM_MOCK=true 且其它源都没配时生效

    目的只有一个 —— 让「没有真实 API Key」的机器也能把
    网关 -> 提示词 -> 解析 -> 返回 这条链路完整跑一遍。
    返回的是空结构，数据没有业务含义，严禁用于生产。
    """

    name = "mock"

    async def load(self, tenant_id: str = "000000") -> list[ModelConfig]:
        if not settings.llm_mock:
            return []
        logger.warning("MOCK 模型源生效：不会产生真实模型调用，仅供联调自测")
        return [
            ModelConfig(
                code="mock",
                name="MOCK（未配置真实模型）",
                model_name="mock-model",
                base_url="http://127.0.0.1/mock/v1",
                api_key="mock",
                temperature=settings.llm_temperature,
                max_tokens=settings.llm_max_tokens,
                timeout=settings.llm_timeout,
                retry_count=0,
                priority=999,
                tenant_id=tenant_id,
                source="mock",
            )
        ]


class ModelRegistry:
    """配置注册中心：按优先级串起多个源，带缓存"""

    def __init__(self) -> None:
        self._providers: dict[str, ModelProvider] = {
            "mysql": MysqlModelProvider(),
            "env": EnvModelProvider(),
            "mock": MockModelProvider(),
        }
        self._mock_on: bool = False  # 由 llm_mock 决定，运行时可查
        self._cache: dict[str, tuple[float, list[ModelConfig]]] = {}
        self._lock = asyncio.Lock()

    def _source_chain(self) -> list[str]:
        """配置源链：AGENT_MODEL_SOURCE 里配的 + MOCK 开关自动兜底"""
        chain = [s.strip() for s in settings.model_source.split(",") if s.strip()]
        if settings.llm_mock and "mock" not in chain:
            chain.append("mock")
        self._mock_on = settings.llm_mock
        return chain

    async def list_models(self, tenant_id: str = "000000", force: bool = False) -> list[ModelConfig]:
        now = time.time()
        if not force:
            cached = self._cache.get(tenant_id)
            if cached and now - cached[0] < settings.model_cache_ttl:
                return cached[1]

        async with self._lock:
            # 双重检查，避免并发重复加载
            cached = self._cache.get(tenant_id)
            if not force and cached and time.time() - cached[0] < settings.model_cache_ttl:
                return cached[1]

            merged: list[ModelConfig] = []
            seen: set[str] = set()
            for src in self._source_chain():
                provider = self._providers.get(src)
                if provider is None:
                    continue
                for cfg in await provider.load(tenant_id):
                    # 同名模型去重，靠前的源优先
                    key = f"{cfg.model_name}@{cfg.base_url}"
                    if key in seen:
                        continue
                    seen.add(key)
                    merged.append(cfg)
                if merged:
                    # 该源拿到了配置就停止（mysql 为空才会落到 env）
                    break

            merged.sort(key=lambda c: (c.priority, c.code))
            self._cache[tenant_id] = (time.time(), merged)
            return merged

    def invalidate(self, tenant_id: str | None = None) -> None:
        if tenant_id:
            self._cache.pop(tenant_id, None)
        else:
            self._cache.clear()

    def source_status(self) -> dict[str, Any]:
        out = {}
        for name, p in self._providers.items():
            entry: dict[str, Any] = {"name": name}
            if isinstance(p, MysqlModelProvider):
                entry["enabled"] = settings.mysql_enabled
                entry["last_error"] = p.last_error
            if isinstance(p, MockModelProvider):
                entry["enabled"] = settings.llm_mock
            out[name] = entry
        out["chain"] = self._source_chain()
        return out


registry = ModelRegistry()
