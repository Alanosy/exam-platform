"""质检 Skill（Reflection 模式的 Critic）

这是 Reflection 模式的落点：出题之后立刻自检一遍。
只多一次调用，就能拦掉「答案不唯一」「选项长度暗示」这类致命问题。
"""

from __future__ import annotations

from typing import Any, ClassVar

from app.llm.structured import parse_model
from app.skills.base import READ, AiSkill, SkillContext, as_text
from app.skills.schemas import QuestionAuditInput, QuestionAuditOutput


class QuestionAuditSkill(AiSkill):
    """试题质检：输出问题清单与严重度"""

    code: ClassVar[str] = "question_audit"
    name: ClassVar[str] = "试题质检"
    description: ClassVar[str] = "审查单道试题的答案唯一性、歧义、暗示泄露等问题"
    prompt_code: ClassVar[str] = "question_audit"
    risk_level: ClassVar[str] = READ

    @property
    def input_model(self) -> type[QuestionAuditInput]:
        return QuestionAuditInput

    def build_vars(self, inp: QuestionAuditInput, ctx: SkillContext) -> dict[str, Any]:
        return {
            "question_type": inp.question_type or "（未标注）",
            "difficulty": inp.difficulty or "（未标注）",
            "knowledge_points": "、".join(inp.knowledge_points or []) or "（未标注）",
            "stem": inp.stem,
            "options": as_text([f"{o.key}. {o.content}" for o in (inp.options or [])]),
            "answer": inp.answer or "（无）",
            "analysis": inp.analysis or "（无）",
            "rag_context": inp.rag_context or "（无）",
        }

    def parse(self, content: str) -> QuestionAuditOutput:
        return parse_model(content, QuestionAuditOutput)

    async def audit_batch(
        self, items: list[QuestionAuditInput], ctx: SkillContext
    ) -> list[QuestionAuditOutput]:
        """批量质检：并发跑，单条失败不影响其它（质检本身要容错）"""
        import asyncio

        async def one(item: QuestionAuditInput) -> QuestionAuditOutput | None:
            try:
                result = await self.run(item, ctx)
                return result.data  # type: ignore[return-value]
            except Exception as e:  # noqa: BLE001
                from app.core.logging import logger

                logger.warning("试题质检失败，跳过该题: %s", e)
                return None

        results = await asyncio.gather(*[one(i) for i in items])
        return [r for r in results if r is not None]
