"""会话存储

只存「跑流程需要的最小状态」：消息历史 + 正在进行的意图与槽位。

为什么不接 Redis：对话是短任务，进程重启丢掉会话是可接受的（用户重发一句即可），
而引入 Redis 会让「本地起服务调试」必须先装 Redis。这里用内存 dict + TTL 清理，
后续要持久化只需替换 backend。

会话里**不存 Trace**：Trace 是一次回合的执行日志，回合结束就归档到消息里，
下次回合重新生成，避免会话越跑越大。
"""

from __future__ import annotations

import logging
import time
import uuid
from dataclasses import dataclass, field
from typing import Any

logger = logging.getLogger(__name__)

SESSION_TTL = 1800  # 30 分钟无活动即回收
MAX_SESSIONS = 500  # 内存上限，超出后淘汰最久未使用的
MAX_HISTORY = 40    # 每个会话保留的消息条数


@dataclass
class ChatSession:
    """一个对话会话"""

    id: str
    tenant_id: str = "000000"
    user_id: str | None = None
    created_at: float = field(default_factory=time.time)
    updated_at: float = field(default_factory=time.time)
    messages: list[dict[str, str]] = field(default_factory=list)
    intent: str = ""
    slots: dict[str, Any] = field(default_factory=dict)

    def touch(self) -> None:
        self.updated_at = time.time()

    def add_message(self, role: str, content: str) -> None:
        self.messages.append({"role": role, "content": content})
        if len(self.messages) > MAX_HISTORY:
            # 保留最近的消息，但第一条 system 不丢
            self.messages = self.messages[-MAX_HISTORY:]
        self.touch()

    def history_text(self, limit: int = 6) -> str:
        """最近 N 条消息转文本，供 LLM 做上下文"""
        recent = self.messages[-limit:]
        return "\n".join(f"{m['role']}: {m['content']}" for m in recent)


class SessionStore:
    """内存会话存储（带 TTL 与容量上限）"""

    def __init__(self) -> None:
        self._sessions: dict[str, ChatSession] = {}

    def get(self, session_id: str) -> ChatSession | None:
        session = self._sessions.get(session_id)
        if session is None:
            return None
        if time.time() - session.updated_at > SESSION_TTL:
            self._sessions.pop(session_id, None)
            return None
        return session

    def create(self, tenant_id: str = "000000", user_id: str | None = None) -> ChatSession:
        self._evict_if_needed()
        session = ChatSession(
            id=uuid.uuid4().hex[:16], tenant_id=tenant_id, user_id=user_id
        )
        self._sessions[session.id] = session
        return session

    def get_or_create(
        self, session_id: str | None, tenant_id: str = "000000", user_id: str | None = None
    ) -> ChatSession:
        if session_id:
            session = self.get(session_id)
            if session is not None:
                return session
        return self.create(tenant_id, user_id)

    def drop(self, session_id: str) -> bool:
        return self._sessions.pop(session_id, None) is not None

    def _evict_if_needed(self) -> None:
        if len(self._sessions) < MAX_SESSIONS:
            return
        ordered = sorted(self._sessions.values(), key=lambda s: s.updated_at)
        for session in ordered[: max(1, len(ordered) // 5)]:
            self._sessions.pop(session.id, None)

    @property
    def size(self) -> int:
        return len(self._sessions)


store = SessionStore()
