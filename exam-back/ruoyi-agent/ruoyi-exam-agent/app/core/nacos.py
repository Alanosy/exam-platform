"""Nacos 服务注册与心跳

要点：
1. **注册 ≠ 活着**。nacos-sdk-python 的 add_naming_instance 只发一次请求，
   临时实例（ephemeral）靠心跳续约，所以这里起了一个后台心跳线程。
2. **注册失败不能拖垮服务**：Nacos 不可用时以「未注册」状态继续运行，
   通过 /health 与 /api/ai/model/health 暴露状态，由运维发现。
3. 开启了鉴权的 Nacos 必须传 username/password，否则返回 403 user not found。
"""

from __future__ import annotations

import logging
import socket
import threading
import time
from typing import Any

import nacos

from app.config import settings

logger = logging.getLogger(__name__)

_client: nacos.NacosClient | None = None
_registered_ip: str = ""
_stop_event = threading.Event()
_heartbeat_thread: threading.Thread | None = None
_state: dict[str, Any] = {"registered": False, "error": None, "service": settings.service_name}


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


def _beat_loop() -> None:
    """心跳续约循环"""
    interval = max(2, settings.nacos_heartbeat_interval)
    while not _stop_event.wait(interval):
        if _client is None or not _registered_ip:
            continue
        try:
            _client.send_heartbeat(
                service_name=settings.service_name,
                ip=_registered_ip,
                port=settings.port,
                cluster_name=settings.nacos_cluster,
                group_name=settings.nacos_group,
            )
        except Exception as e:  # noqa: BLE001
            logger.warning("Nacos 心跳失败: %s", e)
            # 心跳失败通常意味着实例已被摘除，尝试重新注册一次
            try:
                _do_register()
            except Exception as re:  # noqa: BLE001
                logger.warning("Nacos 重新注册失败: %s", re)


def _do_register() -> None:
    global _client, _registered_ip
    if _client is None:
        _client = nacos.NacosClient(
            settings.nacos_server,
            namespace=settings.nacos_namespace,
            username=settings.nacos_username or None,
            password=settings.nacos_password or None,
        )
    _registered_ip = _get_local_ip()
    _client.add_naming_instance(
        service_name=settings.service_name,
        ip=_registered_ip,
        port=settings.port,
        cluster_name=settings.nacos_cluster,
        group_name=settings.nacos_group,
        ephemeral=True,
        metadata={
            "version": "1.0.0",
            "lang": "python",
            "framework": "fastapi",
            "health": f"http://{_registered_ip}:{settings.port}/health",
        },
    )


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
        logger.info("Nacos 注册成功: %s %s:%s (group=%s, ns=%s)",
                    settings.service_name, _registered_ip, settings.port,
                    settings.nacos_group, settings.nacos_namespace)

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
    if _client is None or not _registered_ip:
        return
    try:
        _client.remove_naming_instance(
            service_name=settings.service_name,
            ip=_registered_ip,
            port=settings.port,
            cluster_name=settings.nacos_cluster,
            group_name=settings.nacos_group,
        )
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
        **_state,
    }
