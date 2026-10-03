"""模型网关单测

三件事必须守住：
1. 配置源能降级（MySQL 没配 -> env -> mock），服务不能因为没配模型就起不来
2. 熔断真的会熔断（否则挂掉的模型会被每个请求试一遍，批量阅卷直接卡死）
3. 全部候选失败必须抛异常，**绝不静默降级成假成功**
"""

from __future__ import annotations

import time

import pytest

from app.core.errors import AiUnavailableError, ModelConfigError
from app.gateway.circuit import CircuitBreaker, State
from app.gateway.models import ChatMessage, ChatRequest
from app.gateway.providers import MockModelProvider, registry as provider_registry
from app.gateway.router import router


def test_circuit_healthy_by_default():
    cb = CircuitBreaker(fail_threshold=3, cooldown=60)
    assert cb.allow("m1") is True
    assert cb.snapshot("m1")["state"] == State.HEALTHY.value


def test_circuit_opens_after_threshold():
    cb = CircuitBreaker(fail_threshold=3, cooldown=60)
    for _ in range(3):
        cb.record_failure("m1", "boom")
    assert cb.allow("m1") is False
    assert cb.snapshot("m1")["state"] == State.OPEN.value


def test_circuit_half_open_after_cooldown():
    cb = CircuitBreaker(fail_threshold=1, cooldown=1)
    cb.record_failure("m1", "boom")
    assert cb.allow("m1") is False

    cb._stats["m1"].opened_at = time.time() - 5  # 伪造冷却已过
    assert cb.allow("m1") is True
    assert cb.snapshot("m1")["state"] == State.HALF_OPEN.value


def test_circuit_recovers_on_success():
    cb = CircuitBreaker(fail_threshold=1, cooldown=60)
    cb.record_failure("m1", "boom")
    cb._stats["m1"].opened_at = time.time() - 100
    cb.allow("m1")
    cb.record_success("m1")
    assert cb.snapshot("m1")["state"] == State.HEALTHY.value


def test_circuit_isolated_per_model():
    """一个模型熔断不能影响其它模型"""
    cb = CircuitBreaker(fail_threshold=1, cooldown=60)
    cb.record_failure("bad", "boom")
    assert cb.allow("bad") is False
    assert cb.allow("good") is True


@pytest.mark.asyncio
async def test_mock_provider_yields_candidate():
    cfgs = await MockModelProvider().load("000000")
    assert len(cfgs) == 1
    assert cfgs[0].source == "mock"


@pytest.mark.asyncio
async def test_registry_degrades_to_mock():
    """MySQL 关掉 + env 没配 Key -> 必须落到 mock，而不是返回空"""
    cfgs = await provider_registry.list_models("000000", force=True)
    assert cfgs, "配置源全空会导致所有 Skill 不可用"
    assert cfgs[0].source == "mock"


@pytest.mark.asyncio
async def test_router_chat_success_in_mock_mode():
    """MOCK 返回值必须跟提示词声明的形状一致，否则 object 型 Skill 会解析失败"""

    def req(shape: str) -> ChatRequest:
        return ChatRequest(
            messages=[ChatMessage(role="user", content="hi")],
            response_json=True,
            response_shape=shape,
        )

    assert (await router.chat(req("array"), force_reload=True)).content == "[]"
    assert (await router.chat(req("object"), force_reload=True)).content == "{}"
    resp = await router.chat(req("auto"), force_reload=True)
    assert resp.content in ("[]", "{}")
    assert resp.attempts >= 1


@pytest.mark.asyncio
async def test_router_raises_when_all_candidates_fail(monkeypatch):
    """全部失败必须抛 AiUnavailableError，绝不能静默返回假成功"""

    async def always_fail(cfg, req):  # noqa: ANN001
        from app.llm.client import LlmCallError

        raise LlmCallError("模拟模型不可用")

    monkeypatch.setattr("app.gateway.router.client.chat", always_fail)
    router.circuit.reset()
    provider_registry.invalidate()

    with pytest.raises(AiUnavailableError) as exc:
        await router.chat(ChatRequest(messages=[{"role": "user", "content": "x"}]))  # type: ignore[list-item]

    assert "所有候选模型均调用失败" in str(exc.value)


@pytest.mark.asyncio
async def test_unknown_model_code_is_rejected():
    from app.gateway.models import ChatMessage, ChatRequest

    with pytest.raises(ModelConfigError):
        await router.chat(
            ChatRequest(messages=[ChatMessage(role="user", content="x")], model_code="不存在的模型"),
            force_reload=True,
        )
