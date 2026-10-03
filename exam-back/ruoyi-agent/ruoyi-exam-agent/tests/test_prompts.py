"""提示词层单测

重点验证「渲染安全」：提示词里塞满了 JSON 示例（{"score": 8}），
如果用 str.format 会把花括号吃掉，这里专门守住这条底线。
"""

from __future__ import annotations

from app.prompts.registry import registry


def test_prompts_loaded():
    assert registry.size >= 17, f"提示词只加载了 {registry.size} 条，检查 prompts/ 目录"


def test_every_prompt_has_required_fields():
    for tpl in registry._templates.values():
        assert tpl.code, "模板缺少 code"
        assert tpl.system.strip(), f"{tpl.code} 缺少 system"
        assert tpl.user.strip(), f"{tpl.code} 缺少 user"
        assert tpl.version, f"{tpl.code} 缺少 version"


def test_render_replaces_placeholder():
    tpl = registry.require("mark_score")
    system, user = tpl.render(stem="简述 TCP 三次握手", full_score=10, answer_text="SYN...")
    assert "简述 TCP 三次握手" in user
    assert "{stem}" not in user


def test_render_keeps_json_braces():
    """JSON 示例里的花括号必须原样保留，这是不用 str.format 的原因"""
    tpl = registry.require("mark_score")
    system, _ = tpl.render(stem="x")
    assert '{"score": 7.5' in system or '"score":' in system


def test_render_missing_var_is_kept_not_crash():
    tpl = registry.require("question_gen")
    _, user = tpl.render(count=3)  # 故意只传一个变量
    assert "{difficulty}" in user, "缺失变量应原样保留，便于排查模板改动"


def test_render_list_value():
    tpl = registry.require("question_gen")
    _, user = tpl.render(knowledge_points=["TCP", "UDP"], count=1)
    assert "TCP" in user
