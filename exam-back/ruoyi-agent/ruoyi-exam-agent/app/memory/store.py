"""记忆层

短期记忆：会话上下文（组卷中间态、多轮对话），TTL 过期
长期记忆：学情画像 / 评分偏好 / 租户出题风格

Redis 连不上时自动降级为进程内内存实现 —— 单机开发、以及客户环境没有 Redis
时服务照样能起来，只是重启后记忆丢失。
"""

from __future__ import annotations

import json
import logging
import time
from typing import Any

from app.config import settings

logger = logging.getLogger(__name__)


class MemoryBackend:
    """记忆后端抽象"""

    name = "base"

    async def get(self, key: str) -> Any | None:
        raise NotImplementedError

    async def set(self, key: str, value: Any, ttl: int | None = None) -> None:
        raise NotImplementedError

    async def delete(self, key: str) -> None:
        raise NotImplementedError

    async def hgetall(self, key: str) -> dict[str, Any]:
        raise NotImplementedError

    async def hset(self, key: str, field: str, value: Any) -> None:
        raise NotImplementedError


class InMemoryBackend(MemoryBackend):
    """进程内内存实现（降级用）"""

    name = "memory"

    def __init__(self) -> None:
        self._kv: dict[str, tuple[float, Any]] = {}
        self._hash: dict[str, dict[str, Any]] = {}

    async def get(self, key: str) -> Any | None:
        item = self._kv.get(key)
        if not item:
            return None
        expire, value = item
        if expire and time.time() > expire:
            self._kv.pop(key, None)
            return None
        return value

    async def set(self, key: str, value: Any, ttl: int | None = None) -> None:
        self._kv[key] = (time.time() + ttl if ttl else 0, value)

    async def delete(self, key: str) -> None:
        self._kv.pop(key, None)
        self._hash.pop(key, None)

    async def hgetall(self, key: str) -> dict[str, Any]:
        return dict(self._hash.get(key) or {})

    async def hset(self, key: str, field: str, value: Any) -> None:
        self._hash.setdefault(key, {})[field] = value


class RedisBackend(MemoryBackend):
    """Redis 实现"""

    name = "redis"

    def __init__(self) -> None:
        import redis.asyncio as aioredis  # 局部导入，没装 redis 也能跑

        self._client = aioredis.Redis(
            host=settings.redis_host,
            port=settings.redis_port,
            password=settings.redis_password or None,
            db=settings.redis_db,
            decode_responses=True,
            socket_connect_timeout=2,
        )

    async def get(self, key: str) -> Any | None:
        raw = await self._client.get(key)
        return json.loads(raw) if raw else None

    async def set(self, key: str, value: Any, ttl: int | None = None) -> None:
        payload = json.dumps(value, ensure_ascii=False)
        if ttl:
            await self._client.setex(key, ttl, payload)
        else:
            await self._client.set(key, payload)

    async def delete(self, key: str) -> None:
        await self._client.delete(key)

    async def hgetall(self, key: str) -> dict[str, Any]:
        raw = await self._client.hgetall(key) or {}
        return {k: json.loads(v) for k, v in raw.items()}

    async def hset(self, key: str, field: str, value: Any) -> None:
        await self._client.hset(key, field, json.dumps(value, ensure_ascii=False))


_backend: MemoryBackend | None = None


def get_memory() -> MemoryBackend:
    """获取记忆后端（只初始化一次）"""
    global _backend
    if _backend is not None:
        return _backend

    if settings.memory_backend in ("auto", "redis"):
        try:
            _backend = RedisBackend()
            logger.info("记忆层使用 Redis %s:%s", settings.redis_host, settings.redis_port)
            return _backend
        except Exception as e:  # noqa: BLE001
            logger.warning("Redis 不可用，记忆层降级为内存实现: %s", e)

    _backend = InMemoryBackend()
    logger.info("记忆层使用进程内内存（重启后丢失）")
    return _backend


class MemoryService:
    """面向业务的记忆封装"""

    def __init__(self) -> None:
        self.backend = get_memory()

    async def short_term(self, session_id: str) -> dict[str, Any]:
        return await self.backend.get(f"st:{session_id}") or {}

    async def save_short_term(self, session_id: str, data: dict[str, Any]) -> None:
        await self.backend.set(f"st:{session_id}", data, ttl=settings.memory_ttl)

    async def clear_short_term(self, session_id: str) -> None:
        await self.backend.delete(f"st:{session_id}")

    async def profile(self, tenant_id: str, user_id: str) -> dict[str, Any]:
        """学情画像（长期记忆）"""
        return await self.backend.hgetall(f"lt:profile:{tenant_id}:{user_id}")

    async def update_profile(self, tenant_id: str, user_id: str, field: str, value: Any) -> None:
        await self.backend.hset(f"lt:profile:{tenant_id}:{user_id}", field, value)

    async def scoring_style(self, tenant_id: str, marker: str) -> dict[str, Any]:
        """阅卷风格（某位老师历来偏松/偏紧）"""
        return await self.backend.hgetall(f"lt:scoring:{tenant_id}:{marker}")

    async def update_scoring_style(
        self, tenant_id: str, marker: str, field: str, value: Any
    ) -> None:
        await self.backend.hset(f"lt:scoring:{tenant_id}:{marker}", field, value)


memory = MemoryService()
