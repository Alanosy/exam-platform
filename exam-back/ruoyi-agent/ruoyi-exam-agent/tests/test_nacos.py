"""Nacos 注册层单测

这里守的是一条踩过的坑：**nacos-sdk-python 在开启鉴权的 Nacos 上注册必失败**
（报 `Insufficient privilege.`，极具误导性），所以本项目改成 REST 直连。
如果哪天有人把实现换回 SDK，这个文件的注释就是留给他的警示。
"""

from __future__ import annotations

from app.core import nacos


def test_state_exposes_mode_and_flag():
    s = nacos.nacos_state()
    assert s["mode"] == "rest", "注册方式变了请同步更新文档与运维预期"
    assert "registered" in s
    assert s["service"] == "ruoyi-exam-agent"


def test_local_ip_is_ipv4():
    ip = nacos._get_local_ip()
    assert ip.count(".") == 3, f"拿到的不是 IPv4: {ip}"
    assert ip != "0.0.0.0"


def test_beat_payload_shape():
    """心跳体的 serviceName 必须是 groupName@@serviceName，否则 Nacos 认不出"""
    nacos._registered_ip = "1.2.3.4"
    payload = nacos._beat_payload()
    assert "@@" in payload, f"serviceName 缺少 group 前缀: {payload}"
    assert "1.2.3.4" in payload


def test_disabled_state_is_reported_not_crashed():
    """Nacos 关掉时服务必须照常运行，只是状态标为未注册"""
    import asyncio

    from app.config import settings

    old = settings.nacos_enabled
    settings.nacos_enabled = False
    try:
        asyncio.run(nacos.register_to_nacos())
        s = nacos.nacos_state()
        assert s["registered"] is False
        assert "禁用" in (s["error"] or "")
    finally:
        settings.nacos_enabled = old
