"""熔断器状态机

为什么必须有熔断（架构文档 6.3）：
    没有熔断时，一个挂掉的模型会被**每个请求都试一遍**。
    一场 200 人的批量阅卷会白白浪费 200 次超时等待（每次 60s），
    整个任务直接卡死。

状态迁移：
                连续失败 >= fail_threshold
    HEALTHY ─────────────────────────────► OPEN（熔断，冷却 cooldown 秒）
       ▲                                      │ 冷却结束
       │        探针成功                       ▼
       └──────────────────── HALF_OPEN（放行 1 个探针）
                                 │ 探针失败 → 回 OPEN
"""

from __future__ import annotations

import logging
import threading
import time
from dataclasses import dataclass, field
from enum import Enum
from typing import Any

logger = logging.getLogger(__name__)


class State(str, Enum):
    HEALTHY = "HEALTHY"
    OPEN = "OPEN"
    HALF_OPEN = "HALF_OPEN"


@dataclass
class CircuitStats:
    """单个模型的熔断统计"""

    state: State = State.HEALTHY
    consecutive_fail: int = 0
    last_fail_at: float = 0.0
    opened_at: float = 0.0
    total_call: int = 0
    total_fail: int = 0
    last_error: str = ""
    switched_out: int = 0
    _lock: threading.Lock = field(default_factory=threading.Lock, repr=False)


class CircuitBreaker:
    """按模型编码隔离的熔断器"""

    def __init__(self, fail_threshold: int = 3, cooldown: int = 60) -> None:
        self.fail_threshold = max(1, fail_threshold)
        self.cooldown = max(1, cooldown)
        self._stats: dict[str, CircuitStats] = {}
        self._global_lock = threading.Lock()

    def _get(self, code: str) -> CircuitStats:
        with self._global_lock:
            if code not in self._stats:
                self._stats[code] = CircuitStats()
            return self._stats[code]

    def allow(self, code: str) -> bool:
        """当前是否允许向该模型发起调用"""
        st = self._get(code)
        with st._lock:
            if st.state is State.OPEN:
                if time.time() - st.opened_at >= self.cooldown:
                    st.state = State.HALF_OPEN
                    logger.info("模型 %s 冷却结束，进入 HALF_OPEN 探针状态", code)
                    return True
                return False
            return True

    def record_success(self, code: str) -> None:
        st = self._get(code)
        with st._lock:
            st.total_call += 1
            st.consecutive_fail = 0
            st.last_error = ""
            if st.state is not State.HEALTHY:
                logger.info("模型 %s 恢复健康（探针成功）", code)
            st.state = State.HEALTHY

    def record_failure(self, code: str, error: str) -> None:
        st = self._get(code)
        with st._lock:
            st.total_call += 1
            st.total_fail += 1
            st.consecutive_fail += 1
            st.last_fail_at = time.time()
            st.last_error = error[:300]
            if st.consecutive_fail >= self.fail_threshold and st.state is not State.OPEN:
                st.state = State.OPEN
                st.opened_at = time.time()
                logger.warning(
                    "模型 %s 连续失败 %d 次，熔断 %ds: %s",
                    code, st.consecutive_fail, self.cooldown, st.last_error,
                )

    def snapshot(self, code: str) -> dict[str, Any]:
        st = self._get(code)
        with st._lock:
            data = {
                "state": st.state.value,
                "consecutive_fail": st.consecutive_fail,
                "total_call": st.total_call,
                "total_fail": st.total_fail,
                "last_error": st.last_error,
                "switched_out": st.switched_out,
            }
            if st.state is State.OPEN:
                data["cooldown_remain"] = max(
                    0, int(self.cooldown - (time.time() - st.opened_at))
                )
            return data

    def all_snapshot(self) -> dict[str, dict[str, Any]]:
        with self._global_lock:
            codes = list(self._stats.keys())
        return {c: self.snapshot(c) for c in codes}

    def reset(self, code: str | None = None) -> None:
        """手动复位（运维接口用）"""
        if code:
            st = self._get(code)
            with st._lock:
                st.state = State.HEALTHY
                st.consecutive_fail = 0
                st.opened_at = 0.0
        else:
            with self._global_lock:
                self._stats.clear()
