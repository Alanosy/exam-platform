"""提示词注册表

设计要点：
1. **提示词外置**：改提示词不改代码，reload 接口可热加载，
   生产环境配合 `ai_prompt_template` 表（租户级覆写）时能直接下发。
2. **渲染安全**：占位符缺失时保留原样而不是抛异常，
   避免一次模板改动把所有 Skill 打挂。
3. **版本可见**：每个模板带 version，Skill 输出里回传，
   便于追查「这一版分数是哪套提示词打出来的」。
"""

from __future__ import annotations

import logging
import re
from pathlib import Path
from typing import Any

import yaml

logger = logging.getLogger(__name__)

# 只匹配 {identifier}，不匹配 JSON 里的 {"key": value}
_PLACEHOLDER = re.compile(r"\{([a-zA-Z_][a-zA-Z0-9_]*(?:\.[a-zA-Z0-9_]+)*)\}")


def _safe_render(text: str, ctx: dict[str, Any]) -> str:
    """按上下文替换占位符，缺失则原样保留"""

    def repl(m: re.Match[str]) -> str:
        key = m.group(1)
        if key in ctx:
            value = ctx[key]
            if isinstance(value, (list, dict)):
                import json

                return json.dumps(value, ensure_ascii=False)
            return "" if value is None else str(value)
        # 支持 a.b 取值
        if "." in key:
            cur: Any = ctx
            for part in key.split("."):
                if isinstance(cur, dict) and part in cur:
                    cur = cur[part]
                else:
                    return m.group(0)
            return str(cur)
        return m.group(0)

    return _PLACEHOLDER.sub(repl, text or "")


class PromptTemplate:
    """一条提示词模板"""

    def __init__(self, data: dict[str, Any], source: str = "") -> None:
        self.code: str = data.get("code", "")
        self.name: str = data.get("name", self.code)
        self.version: str = str(data.get("version", "v1"))
        self.description: str = data.get("description", "")
        self.system: str = data.get("system", "")
        self.user: str = data.get("user", "")
        self.params: dict[str, Any] = data.get("params", {}) or {}
        self.source = source

    def render(self, **kwargs: Any) -> tuple[str, str]:
        """渲染 system / user，缺失的占位符原样保留

        刻意**不用** str.format：提示词里大量出现 JSON 示例（{"score": 8}），
        format 会把花括号吃掉、还要求写 {{ }} 转义，极易出错。
        这里只替换形如 {identifier} 的占位符，其余花括号原样保留。
        """
        return _safe_render(self.system, kwargs), _safe_render(self.user, kwargs)

    def public_dict(self) -> dict[str, Any]:
        return {
            "code": self.code,
            "name": self.name,
            "version": self.version,
            "description": self.description,
            "params": self.params,
            "source": self.source,
        }


class PromptRegistry:
    def __init__(self, directory: str = "prompts") -> None:
        self._dir = Path(directory)
        self._templates: dict[str, PromptTemplate] = {}

    def load(self) -> int:
        """扫描目录加载所有 *.yaml / *.yml"""
        self._templates.clear()
        if not self._dir.exists():
            logger.warning("提示词目录不存在: %s", self._dir.resolve())
            return 0

        count = 0
        for path in sorted(self._dir.glob("*.y*ml")):
            try:
                data = yaml.safe_load(path.read_text(encoding="utf-8")) or {}
            except Exception as e:  # noqa: BLE001
                logger.warning("提示词文件解析失败 %s: %s", path.name, e)
                continue
            if not isinstance(data, dict) or not data.get("code"):
                logger.warning("提示词文件缺少 code 字段，跳过: %s", path.name)
                continue
            tpl = PromptTemplate(data, source=path.name)
            self._templates[tpl.code] = tpl
            count += 1
        logger.info("已加载 %d 条提示词模板", count)
        return count

    def get(self, code: str) -> PromptTemplate | None:
        return self._templates.get(code)

    def require(self, code: str) -> PromptTemplate:
        tpl = self._templates.get(code)
        if tpl is None:
            from app.core.errors import SkillNotFoundError

            raise SkillNotFoundError(f"未找到提示词模板: {code}")
        return tpl

    def list_all(self) -> list[dict[str, Any]]:
        return [t.public_dict() for t in self._templates.values()]

    def reload(self) -> int:
        return self.load()

    @property
    def size(self) -> int:
        return len(self._templates)


registry = PromptRegistry()
