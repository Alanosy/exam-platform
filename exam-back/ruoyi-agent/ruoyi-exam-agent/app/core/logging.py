"""日志与调用观测

两层：
1. 标准 logging：给运维看
2. CallLog：环形缓冲的 AI 调用记录，给 /api/ai/model/call-log 与 /health 看，
   用于排查「主模型是不是挂了、切了几次、耗了多少 token」
"""

from __future__ import annotations

import logging
import sys
import time
import threading
from collections import deque
from dataclasses import dataclass, asdict
from typing import Any

from app.config import settings


def setup_logging() -> None:
    handler = logging.StreamHandler(sys.stdout)
    handler.setFormatter(
        logging.Formatter(
            fmt="%(asctime)s %(levelname)-7s [%(name)s] %(message)s",
            datefmt="%H:%M:%S",
        )
    )
    root = logging.getLogger()
    root.handlers.clear()
    root.addHandler(handler)
    root.setLevel(getattr(logging, settings.log_level.upper(), logging.INFO))
    # 第三方库降噪
    for noisy in ("httpx", "httpcore", "nacos", "urllib3"):
        logging.getLogger(noisy).setLevel(logging.WARNING)


@dataclass
class CallRecord:
    """一次 AI 调用的流水"""

    ts: float
    biz_type: str
    skill_code: str
    model: str
    attempt_chain: str
    success: bool
    latency_ms: int
    prompt_tokens: int | None = None
    completion_tokens: int | None = None
    error: str | None = None


class CallLog:
    """内存环形调用日志（进程内，重启即失）

    生产环境若要持久化，把 append() 里的内容顺手写到 MySQL 的 ai_call_log 表即可，
    本模块刻意不引入 DB 写依赖，保持「只读数据库」的边界。
    """

    def __init__(self, size: int = 500) -> None:
        self._buf: deque[CallRecord] = deque(maxlen=size)
        self._lock = threading.Lock()
        self.total = 0
        self.failed = 0
        self.switched = 0

    def append(self, record: CallRecord) -> None:
        with self._lock:
            self._buf.append(record)
            self.total += 1
            if not record.success:
                self.failed += 1
            if ">" in record.attempt_chain:
                self.switched += 1

    def recent(self, limit: int = 50) -> list[dict[str, Any]]:
        with self._lock:
            items = list(self._buf)[-limit:]
        return [asdict(i) for i in items]

    def stats(self) -> dict[str, Any]:
        with self._lock:
            return {
                "total": self.total,
                "failed": self.failed,
                "switched": self.switched,
                "success_rate": round(1 - self.failed / self.total, 4) if self.total else None,
            }


call_log = CallLog(size=settings.call_log_size)


class Timer:
    """简单的耗时统计上下文"""

    def __init__(self) -> None:
        self.start = time.perf_counter()
        self.elapsed_ms = 0

    def __enter__(self) -> "Timer":
        return self

    def __exit__(self, *exc: Any) -> None:
        self.elapsed_ms = int((time.perf_counter() - self.start) * 1000)
