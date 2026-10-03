"""接口层单测：走 FastAPI TestClient，不启真实端口

重点验证统一返回体 R<T> 的形状 —— 之前出过的坑是接口标注 `-> dict`
却返回 Pydantic 对象，FastAPI 校验失败导致所有接口 data 为空。
"""

from __future__ import annotations


from app.tools.catalog import TOOLS


def _data(resp) -> dict:
    """业务接口统一返回 R<T>"""
    body = resp.json()
    assert body["code"] == 200, f"业务码非 200: {body}"
    assert "data" in body
    return body["data"]


def test_health(client):
    # /health 刻意不包 R<T>：Nacos / Docker HEALTHCHECK 直接读 status 字段
    d = client.get("/health").json()
    assert d["status"] == "UP"
    assert d["skills"] >= 17
    assert d["prompts"] >= 17
    assert d["nacos"]["registered"] is False, "单测环境关掉了 Nacos"


def test_ping(client):
    assert client.get("/ping").json()["pong"] is True


def test_skill_list(client):
    d = _data(client.get("/api/ai/skill/list"))
    assert d["count"] >= 17
    codes = {i["code"] for i in d["items"]}
    assert "mark_score" in codes
    assert "question_gen" in codes


def test_skill_detail(client):
    d = _data(client.get("/api/ai/skill/mark_score"))
    assert d["code"] == "mark_score"
    assert "input_schema" in d


def test_skill_detail_not_found(client):
    body = client.get("/api/ai/skill/not_exist").json()
    assert body["code"] != 200


def test_skill_run(client):
    body = client.post(
        "/api/ai/skill/mark_score/run",
        json={
            "input": {
                "stem": "简述 TCP 三次握手",
                "full_score": 10,
                "standard_answer": "SYN -> SYN-ACK -> ACK",
                "answer_text": "客户端发 SYN，服务端回 SYN-ACK，客户端再发 ACK",
            }
        },
    ).json()
    assert body["code"] == 200, body
    assert body["data"]["skill"] == "mark_score"
    assert body["data"]["prompt_version"]


def test_skill_run_bad_input(client):
    body = client.post("/api/ai/skill/mark_score/run", json={"input": {"full_score": 10}}).json()
    assert body["code"] != 200, "缺必填字段应返回业务错误而不是 500"


def test_skill_batch(client):
    body = client.post(
        "/api/ai/skill/mark_score/batch",
        json={
            "items": [
                {"input": {"stem": "s", "full_score": 10, "standard_answer": "a", "answer_text": "b"}},
                {"input": {"full_score": 10}},
            ],
            "skip_error": True,
        },
    ).json()
    d = body["data"]
    assert d["total"] == 2
    assert d["success"] == 1
    assert d["failed"] == 1


def test_tool_list(client):
    d = _data(client.get("/api/ai/tool/list"))
    assert d["count"] == len(TOOLS)
    assert "submit_mark_score" in d["guardrail"]["high_risk_tools"]


def test_tool_guardrail_blocks_high_risk(client):
    """不批准就调 submit_mark_score 必须被护栏拦下"""
    d = _data(
        client.post(
            "/api/ai/tool/submit_mark_score/invoke",
            json={"payload": {"item_id": "1", "score": 5}},
        )
    )
    assert d["ok"] is False
    assert d.get("blocked") is True


def test_model_list(client):
    d = _data(client.get("/api/ai/model/list"))
    assert d["count"] >= 1
    assert d["models"][0]["source"] == "mock"
    assert "api_key" not in d["models"][0], "密钥绝不能出现在接口响应里"
    assert d["models"][0]["api_key_masked"]


def test_agent_plan_list(client):
    body = client.get("/api/ai/agent/plan/list").json()
    assert body["code"] == 200, body


def test_question_generate_compat(client):
    """Java 侧调用地址保持不变"""
    body = client.post(
        "/api/ai/question/generate",
        json={"question_type": "SINGLE", "difficulty": "medium", "knowledge_points": ["TCP"], "count": 2},
    ).json()
    assert body["code"] == 200, body
    assert "questions" in body["data"]


def test_openapi_schema_buildable(client):
    """所有路由的返回类型必须能被 FastAPI 解析（防止 -> dict 与返回类型不一致）"""
    schema = client.get("/openapi.json").json()
    assert schema["paths"], "OpenAPI 为空说明路由注册失败"


def test_chat_accepts_numeric_ids(client):
    """Java 侧的 userId 是 Long、租户号也可能是数字

    不兜这一层 pydantic 直接 422，而 Java 那边只会显示成一句
    「AI 服务暂时不可用」，排查成本很高。
    """
    body = client.post(
        "/api/ai/chat",
        json={"message": "你好", "user_id": 1, "tenant_id": 0, "session_id": None},
    ).json()
    assert body["code"] == 200, f"业务码非 200: {body}"
    assert body["data"]["reply"]


def test_chat_accepts_null_message(client):
    """提交中断卡时前端只回 answers，message 是 null"""
    body = client.post("/api/ai/chat", json={"message": None, "user_id": "1"}).json()
    assert body["code"] == 200, f"业务码非 200: {body}"
    assert body["data"]["reply"]
