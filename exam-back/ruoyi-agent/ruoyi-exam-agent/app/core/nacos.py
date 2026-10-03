"""Nacos 服务注册与心跳（REST 直连）

## 为什么不用 nacos-sdk-python

nacos-sdk-python 0.1.14 在**开启鉴权**的 Nacos 上 `add_naming_instance`
一律返回 `Insufficient privilege.`（实测 dev / public / 空 namespace 全失败），
而**同样的参数走 REST + accessToken 一次就成功**。SDK 的鉴权实现拿不到有效 token，
所以这里直接用 httpx 调 Nacos OpenAPI：

    登录   POST /nacos/v1/auth/users/login      -> accessToken（TTL 18000s）
    注册   POST /nacos/v1/ns/instance
    心跳   PUT  /nacos/v1/ns/instance/beat
    注销   DELETE /nacos/v1/ns/instance

`Insufficient privilege.` 这个报错极具误导性 —— 它让人以为是账号权限不够，
实际是 SDK 压根没把凭据送上去。

## 要点

1. **注册 ≠ 活着**。临时实例（ephemeral）靠心跳续约，注册只是一次性的，
   所以这里起了一个后台心跳线程；心跳失败会尝试重新注册。
2. **注册失败不能拖垮服务**：Nacos 不可用时以「未注册」状态继续运行，
   状态通过 /health 暴露，由运维发现。
3. **注册的 IP 必须能被别的服务访问**。默认取本机出口 IP，
   容器 / 多网卡场景用 `AGENT_NACOS_IP` 显式指定，否则 Java 网关会连到一个不可达的地址。
4. token 会过期，调用遇到 403 时自动重新登录重试一次。
"""

from __future__ import annotations

import json
import logging
import socket
import threading
import time
from typing import Any

import httpx

from app.config import settings

logger = logging.getLogger(__name__)

_stop_event = threading.Event()
_heartbeat_thread: threading.Thread | None = None
_registered_ip: str = ""
_token: str = ""
_token_expire_at: float = 0.0
_state: dict[str, Any] = {
    "registered": False,
    "error": None,
    "service": settings.service_name,
    "mode": "rest",
}


def _get_local_ip() -> str:
    """取本机出口 IP（UDP connect 不发真实包，只为了拿到路由选中的网卡地址）"""
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        try:
            s.connect(("8.8.8.8", 80))
            return s.getsockname()[0]
        finally:
            s.close()
    except Exception:  # noqa: BLE001
        return "127.0.0.1"


def _base_url() -> str:
    return f"http://{settings.nacos_server}"


def _common_params() -> dict[str, Any]:
    return {
        "namespaceId": settings.nacos_namespace,
        "groupName": settings.nacos_group,
        "serviceName": settings.service_name,
    }


def _login(force: bool = False) -> str:
    """换 accessToken。Nacos 未开鉴权时返回空串（此时注册接口不需要 token）"""
    global _token, _token_expire_at
    if _token and not force and time.time() < _token_expire_at:
        return _token

    if not (settings.nacos_username and settings.nacos_password):
        return ""

    try:
        with httpx.Client(timeout=5) as c:
            r = c.post(
                f"{_base_url()}/nacos/v1/auth/users/login",
                data={"username": settings.nacos_username, "password": settings.nacos_password},
            )
        if r.status_code != 200:
            logger.warning("Nacos 登录失败 HTTP %s: %s", r.status_code, r.text[:200])
            return ""
        data = r.json()
        _token = data.get("accessToken", "")
        ttl = int(data.get("tokenTtl") or 18000)
        # 提前 10 分钟刷新，避免心跳刚好卡在过期点
        _token_expire_at = time.time() + max(60, ttl - 600)
        logger.info("Nacos 登录成功，token 有效期 %ss", ttl)
        return _token
    except Exception as e:  # noqa: BLE001
        logger.warning("Nacos 登录异常: %s", e)
        return ""


def _call(method: str, path: str, params: dict[str, Any], retry_on_403: bool = True) -> str:
    """调 Nacos OpenAPI，403 时重新登录后重试一次"""
    token = _login()
    full = dict(params)
    if token:
        full["accessToken"] = token

    url = f"{_base_url()}{path}"
    try:
        with httpx.Client(timeout=5) as c:
            r = c.request(method, url, params=full)
    except httpx.HTTPError as e:
        raise RuntimeError(f"Nacos 不可达: {e}") from e

    if r.status_code == 403 and retry_on_403:
        logger.info("Nacos 返回 403，重新登录后重试")
        full["accessToken"] = _login(force=True)
        with httpx.Client(timeout=5) as c:
            r = c.request(method, url, params=full)

    if r.status_code != 200:
        raise RuntimeError(f"HTTP {r.status_code}: {r.text[:200]}")
    return r.text


def _beat_payload() -> str:
    """心跳体，serviceName 用 groupName@@serviceName 形式"""
    return json.dumps(
        {
            "cluster": settings.nacos_cluster,
            "ip": _registered_ip,
            "port": settings.port,
            "period": max(2000, settings.nacos_heartbeat_interval * 1000),
            "scheduled": True,
            "serviceName": f"{settings.nacos_group}@@{settings.service_name}",
            "weight": 1,
            "ephemeral": True,
            "metadata": {
                "version": "1.0.0",
                "lang": "python",
                "framework": "fastapi",
                "health": f"http://{_registered_ip}:{settings.port}/health",
            },
        },
        ensure_ascii=False,
    )


def _do_register() -> None:
    """注册（对已存在的实例是幂等更新）"""
    global _registered_ip
    _registered_ip = settings.nacos_ip or _get_local_ip()

    params = _common_params()
    params.update(
        {
            "ip": _registered_ip,
            "port": settings.port,
            "clusterName": settings.nacos_cluster,
            "ephemeral": "true",
            "healthy": "true",
            "weight": 1,
            "enabled": "true",
            "metadata": json.dumps(
                {
                    "version": "1.0.0",
                    "lang": "python",
                    "framework": "fastapi",
                    "health": f"http://{_registered_ip}:{settings.port}/health",
                },
                ensure_ascii=False,
            ),
        }
    )
    _call("POST", "/nacos/v1/ns/instance", params)


def _send_beat() -> None:
    params = _common_params()
    params.update({"ip": _registered_ip, "port": settings.port, "beat": _beat_payload()})
    _call("PUT", "/nacos/v1/ns/instance/beat", params)


def _beat_loop() -> None:
    """心跳续约循环。连续失败会尝试重新注册（实例可能已被摘除）"""
    interval = max(2, settings.nacos_heartbeat_interval)
    while not _stop_event.wait(interval):
        if not _registered_ip:
            continue
        try:
            _send_beat()
        except Exception as e:  # noqa: BLE001
            logger.warning("Nacos 心跳失败: %s", e)
            try:
                _do_register()
                _state["registered"] = True
                _state["error"] = None
                logger.info("Nacos 重新注册成功")
            except Exception as re:  # noqa: BLE001
                _state["registered"] = False
                _state["error"] = str(re)
                logger.warning("Nacos 重新注册失败: %s", re)


async def register_to_nacos() -> None:
    """注册到 Nacos 并启动心跳"""
    global _heartbeat_thread
    if not settings.nacos_enabled:
        _state["registered"] = False
        _state["error"] = "已禁用（AGENT_NACOS_ENABLED=false）"
        logger.info("Nacos 注册已禁用，以未注册状态运行")
        return

    try:
        _do_register()
        _state["registered"] = True
        _state["error"] = None
        _state["ip"] = _registered_ip
        _state["port"] = settings.port
        _state["namespace"] = settings.nacos_namespace
        logger.info(
            "Nacos 注册成功: %s %s:%s (group=%s, ns=%s)",
            settings.service_name, _registered_ip, settings.port,
            settings.nacos_group, settings.nacos_namespace,
        )

        _stop_event.clear()
        _heartbeat_thread = threading.Thread(target=_beat_loop, name="nacos-heartbeat", daemon=True)
        _heartbeat_thread.start()
    except Exception as e:  # noqa: BLE001
        _state["registered"] = False
        _state["error"] = str(e)
        logger.warning("Nacos 注册失败，将以未注册状态运行: %s", e)


async def shutdown_nacos() -> None:
    """停止心跳并从 Nacos 注销"""
    _stop_event.set()
    if not _registered_ip or not _state.get("registered"):
        return
    params = _common_params()
    params.update(
        {
            "ip": _registered_ip,
            "port": settings.port,
            "clusterName": settings.nacos_cluster,
        }
    )
    try:
        _call("DELETE", "/nacos/v1/ns/instance", params)
        logger.info("Nacos 注销完成")
    except Exception as e:  # noqa: BLE001
        logger.warning("Nacos 注销失败: %s", e)
    finally:
        _state["registered"] = False


def nacos_state() -> dict[str, Any]:
    """供 /health 暴露注册状态"""
    return {
        "enabled": settings.nacos_enabled,
        "server": settings.nacos_server,
        "namespace": settings.nacos_namespace,
        "group": settings.nacos_group,
        "mode": "rest",
        **_state,
    }
