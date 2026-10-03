"""Skill 层单测

覆盖三件事：
1. 注册完整性（每个 Skill 都有对应提示词、都能给出入参 schema）
2. 端到端能跑（MOCK 模式下网关->提示词->解析->返回 全链路）
3. 契约稳定性（答案字段格式、risk_level 声明）
"""

from __future__ import annotations

import pytest

from app.skills.base import READ, SkillContext, WRITE
from app.skills.registry import registry
from app.prompts.registry import registry as prompt_registry

EXPECTED_CODES = {
    "question_gen", "distractor_gen", "question_rewrite", "knowledge_tag",
    "analysis_gen", "question_audit",
    "mark_score", "mark_reflect", "code_judge", "scoring_calib", "similarity_detect",
    "diagnosis", "recommend",
    "paper_review", "paper_difficulty",
    "proctor_analyze", "report_write",
}


def test_all_skills_registered():
    missing = EXPECTED_CODES - set(registry.codes)
    assert not missing, f"这些 Skill 没注册: {missing}"


def test_every_skill_has_prompt():
    for code in registry.codes:
        skill = registry.get(code)
        prompt_code = skill.prompt_code or skill.code
        assert prompt_registry.get(prompt_code) is not None, f"{code} 找不到提示词 {prompt_code}"


def test_every_skill_has_input_schema():
    for code in registry.codes:
        schema = registry.get(code).input_model.model_json_schema()
        assert schema.get("properties") or schema.get("title"), f"{code} 入参模型为空"


def test_skill_info_exposes_risk_level():
    for code in registry.codes:
        info = registry.get(code).info()
        assert info["risk_level"] in (READ, WRITE), f"{code} risk_level 非法"
        assert info["code"] == code


@pytest.mark.asyncio
async def test_mark_score_end_to_end():
    """MOCK 模式下完整跑一次主观题评分"""
    skill = registry.get("mark_score")
    inp = skill.input_model(
        stem="简述 TCP 三次握手",
        full_score=10,
        standard_answer="SYN -> SYN-ACK -> ACK",
        answer_text="客户端发 SYN，服务端回 SYN-ACK，客户端再发 ACK",
    )
    result = await skill.run(inp, SkillContext(tenant_id="000000"))

    assert result.skill == "mark_score"
    assert result.prompt_version, "回传提示词版本，便于追查是哪套提示词打的分"
    assert result.latency_ms >= 0
    assert result.trace_id
    # MOCK 模式下解析出来是空结构，能解析不报错即可
    assert result.data is not None


@pytest.mark.asyncio
async def test_question_gen_end_to_end():
    skill = registry.get("question_gen")
    inp = skill.input_model(
        question_type="SINGLE", difficulty="medium",
        knowledge_points=["TCP"], count=2, score=5,
    )
    result = await skill.run(inp, SkillContext())
    assert result.skill == "question_gen"
    assert isinstance(result.data, list)


@pytest.mark.asyncio
async def test_batch_run_isolated_failure():
    """批量场景一条失败不能带崩整批"""
    from app.skills.registry import registry as reg

    skill = reg.get("mark_score")
    ok_item = {"stem": "s", "full_score": 10, "standard_answer": "a", "answer_text": "b"}
    bad_item = {"full_score": 10}  # 缺必填字段

    async def one(payload: dict) -> dict:
        try:
            inp = skill.input_model(**payload)
            r = await skill.run(inp, SkillContext())
            return {"ok": True, "data": r.data}
        except Exception as e:  # noqa: BLE001
            return {"ok": False, "error": str(e)}

    results = [await one(ok_item), await one(bad_item)]
    assert results[0]["ok"] is True
    assert results[1]["ok"] is False
