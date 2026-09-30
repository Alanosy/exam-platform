"""Nacos 服务注册（nacos-sdk-python）

启动时将 ruoyi-exam-agent 注册到 Nacos，
Java 网关 ruoyi-exam-ai 通过 Discovery + RestTemplate / OpenFeign 调用本服务。
"""

from __future__ import annotations

import logging
import socket

import nacos

from app.config import settings

logger = logging.getLogger(__name__)

_client: nacos.NacosClient | None = None


def _get_local_ip() -> str:
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
    finally:
        s.close()
    return ip


async def register_to_nacos() -> None:
    """注册到 Nacos"""
    global _client
    try:
        _client = nacos.NacosClient(
            settings.nacos_server,
            namespace=settings.nacos_namespace,
            username=settings.nacos_username,
            password=settings.nacos_password,
        )
        ip = _get_local_ip()
        _client.add_naming_instance(
            service_name="ruoyi-exam-agent",
            ip=ip,
            port=settings.port,
            cluster_name="DEFAULT",
            group_name=settings.nacos_group,
            ephemeral=True,
            metadata={"version": "1.0.0", "lang": "python"},
        )
        logger.info("Nacos 注册成功: ruoyi-exam-agent %s:%s", ip, settings.port)
    except Exception as e:  # noqa: BLE001
        logger.warning("Nacos 注册失败，将以未注册状态运行: %s", e)
        _client = None


async def shutdown_nacos() -> None:
    """从 Nacos 注销"""
    if _client is None:
        return
    try:
        ip = _get_local_ip()
        _client.remove_naming_instance(
            service_name="ruoyi-exam-agent",
            ip=ip,
            port=settings.port,
            cluster_name="DEFAULT",
            group_name=settings.nacos_group,
        )
        logger.info("Nacos 注销完成")
    except Exception as e:  # noqa: BLE001
        logger.warning("Nacos 注销失败: %s", e)
