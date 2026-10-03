"""pytest 全局夹具

关键：**必须在 import app 之前把环境变量设好**。
app.config.settings 是模块级单例，一旦被 import 就固化了配置，
后面再改 os.environ 不会生效。

单测目标：不依赖任何外部服务（MySQL / Nacos / Redis / Java 网关 / 真实模型）。
想跑真实链路的用例用 @pytest.mark.integration 标出来，默认跳过。
"""

from __future__ import annotations

import os
import sys
from pathlib import Path

# ---- 保证 import app 时能找到项目根 ----
ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))
os.chdir(ROOT)  # prompts/ 与 .env 都是相对路径

# ---- 单测环境：全链路走 MOCK，关掉所有外部依赖 ----
os.environ.setdefault("AGENT_LLM_MOCK", "true")
os.environ.setdefault("AGENT_MODEL_SOURCE", "mock")
os.environ.setdefault("AGENT_MYSQL_ENABLED", "false")
os.environ.setdefault("AGENT_NACOS_ENABLED", "false")
os.environ.setdefault("AGENT_TOOLS_ENABLED", "false")
os.environ.setdefault("AGENT_MEMORY_BACKEND", "memory")
os.environ.setdefault("AGENT_RAG_ENABLED", "false")
os.environ.setdefault("AGENT_LOG_LEVEL", "WARNING")

import pytest  # noqa: E402
from fastapi.testclient import TestClient  # noqa: E402

from app.main import app as fastapi_app  # noqa: E402
from app.prompts.registry import registry as prompt_registry  # noqa: E402
from app.skills.registry import registry as skill_registry  # noqa: E402


@pytest.fixture(scope="session", autouse=True)
def _boot():
    """加载提示词与 Skill（lifespan 在 TestClient 里也会跑，这里显式加载一次更可控）"""
    prompt_registry.load()
    skill_registry.load()


@pytest.fixture(scope="session")
def client() -> TestClient:
    """带 lifespan 的测试客户端：会走完整启动流程（不含 Nacos 注册）"""
    with TestClient(fastapi_app) as c:
        yield c
