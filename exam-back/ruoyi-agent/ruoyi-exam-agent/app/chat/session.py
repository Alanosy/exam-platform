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

# 会话级可选项的默认值（前端设置面板可以改）
DEFAULT_OPTIONS: dict[str, Any] = {
    # 每轮带多少条历史消息进模型上下文
    "context_rounds": 6,
    # 写操作（入库 / 发布 / 改分）是否必须先弹确认卡
    "confirm_write": True,
    # 模型编码，留空表示走默认模型
    "model_code": "",
    # 开放域问题是否先规划再执行（关掉就退化成「模型直接回答」）
    "planner": True,
}


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
    # 会话标题：取第一条用户消息，历史会话列表靠它辨认
    title: str = ""
    # 当前登录用户的令牌与 clientid：代调业务接口时以他的身份发出去
    token: str = ""
    client_id: str = ""
    # 身份快照（角色 + 考试域权限），规划阶段用来判断「这个动作他有没有权限」
    identity: dict[str, Any] = field(default_factory=dict)
    # 会话级配置（上下文轮数 / 写操作确认 / 模型），由前端设置面板下发
    options: dict[str, Any] = field(default_factory=lambda: dict(DEFAULT_OPTIONS))

    def touch(self) -> None:
        self.updated_at = time.time()

    def add_message(self, role: str, content: str) -> None:
        self.messages.append({"role": role, "content": content})
        if len(self.messages) > MAX_HISTORY:
            # 保留最近的消息，但第一条 system 不丢
            self.messages = self.messages[-MAX_HISTORY:]
        if not self.title and role == "user" and content.strip():
            self.title = content.strip()[:40]
        self.touch()

    def option(self, key: str, default: Any = None) -> Any:
        return self.options.get(key, default)

    def apply_options(self, incoming: dict[str, Any] | None) -> None:
        """合并前端下发的设置：只认已知键，避免把乱七八糟的东西塞进会话"""
        for key, value in (incoming or {}).items():
            if key in DEFAULT_OPTIONS and value is not None:
                self.options[key] = value
        self.touch()

    def history_text(self, limit: int | None = None) -> str:
        """最近 N 条消息转文本，供 LLM 做上下文

        limit 为空时取会话设置里的 context_rounds（一轮 = 一问一答）。
        """
        if limit is None:
            limit = int(self.option("context_rounds", 6) or 6) * 2
        recent = self.messages[-limit:]
        return "\n".join(f"{m['role']}: {m['content']}" for m in recent)

    def summary(self) -> dict[str, Any]:
        """历史会话列表用的一条记录"""
        return {
            "id": self.id,
            "title": self.title or "（新会话）",
            "intent": self.intent,
            "messageCount": len(self.messages),
            "createdAt": int(self.created_at),
            "updatedAt": int(self.updated_at),
        }


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

    def list_sessions(
        self, tenant_id: str = "000000", user_id: str | None = None, limit: int = 30
    ) -> list[dict[str, Any]]:
        """历史会话列表（按用户隔离：只能看到自己的会话）"""
        items = [
            s.summary()
            for s in self._sessions.values()
            if s.tenant_id == tenant_id
            and (user_id is None or s.user_id == user_id)
            and s.messages
        ]
        items.sort(key=lambda i: i["updatedAt"], reverse=True)
        return items[:limit]

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
