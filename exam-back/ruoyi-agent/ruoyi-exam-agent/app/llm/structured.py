"""结构化输出与 JSON 容错解析

LLM 输出 JSON 时常见的脏格式：
    1. 带 ```json ... ``` 代码块
    2. 前后有「好的，以下是结果：」之类的闲聊
    3. 尾随逗号、单引号、中文引号
    4. 返回的是对象而不是数组（批量场景）

这里统一收敛成一个 parse_json()，失败抛 LlmOutputError（可重试 / 可降级），
不做「解析失败就返回空列表」这种静默降级——阅卷场景下空列表等于 0 分，很危险。
"""

from __future__ import annotations

import json
import logging
import re
from typing import Any, TypeVar

from pydantic import BaseModel, ValidationError

from app.core.errors import LlmOutputError

logger = logging.getLogger(__name__)

T = TypeVar("T", bound=BaseModel)

_CODE_BLOCK = re.compile(r"```(?:json|JSON)?\s*(.*?)```", re.DOTALL)


def strip_code_block(text: str) -> str:
    """去掉 markdown 代码块包裹"""
    m = _CODE_BLOCK.search(text)
    if m:
        return m.group(1).strip()
    cleaned = text.strip()
    if cleaned.startswith("```"):
        cleaned = cleaned[3:]
        if cleaned.lower().startswith("json"):
            cleaned = cleaned[4:]
        cleaned = cleaned.rstrip("`").strip()
    return cleaned


def parse_json(text: str, expect: type | None = None) -> Any:
    """尽力解析 LLM 输出为 JSON

    :param expect: 期望类型（list / dict），不匹配时尝试从对象里取数组字段
    :raises LlmOutputError: 确实解析不出来
    """
    if not text or not text.strip():
        raise LlmOutputError("LLM 返回内容为空")

    cleaned = strip_code_block(text)

    # 第一轮：直接解析
    try:
        data = json.loads(cleaned)
        return _normalize(data, expect)
    except json.JSONDecodeError:
        pass

    # 第二轮：截取第一个 { 或 [ 到最后一个 } 或 ]
    start = min(
        [i for i in (cleaned.find("{"), cleaned.find("[")) if i >= 0],
        default=-1,
    )
    if start >= 0:
        open_ch = cleaned[start]
        close_ch = "}" if open_ch == "{" else "]"
        end = cleaned.rfind(close_ch)
        if end > start:
            snippet = cleaned[start : end + 1]
            try:
                return _normalize(json.loads(snippet), expect)
            except json.JSONDecodeError:
                # 第三轮：清掉尾随逗号再试
                fixed = re.sub(r",\s*([}\]])", r"\1", snippet)
                try:
                    return _normalize(json.loads(fixed), expect)
                except json.JSONDecodeError as e:
                    raise LlmOutputError(
                        f"无法解析 LLM 输出为 JSON: {e}; 原文片段: {cleaned[:200]}"
                    ) from e

    raise LlmOutputError(f"LLM 输出中未找到 JSON 结构，原文片段: {cleaned[:200]}")


def _normalize(data: Any, expect: type | None) -> Any:
    """把解析结果规整成期望的结构"""
    if expect is None:
        return data
    if expect is list:
        if isinstance(data, list):
            return data
        if isinstance(data, dict):
            # 模型常返回 {"questions": [...]} 这种包一层的形式
            for key in ("data", "items", "list", "questions", "results", "answer"):
                v = data.get(key)
                if isinstance(v, list):
                    return v
            # 只有 value 是 list 就取它
            for v in data.values():
                if isinstance(v, list):
                    return v
        raise LlmOutputError(f"期望 JSON 数组，实际得到 {type(data).__name__}")
    if expect is dict:
        if isinstance(data, dict):
            return data
        raise LlmOutputError(f"期望 JSON 对象，实际得到 {type(data).__name__}")
    return data


def parse_model_list(text: str, model_cls: type[T]) -> list[T]:
    """解析为 Pydantic 模型列表，逐项跳过不合法的项并记录日志"""
    raw_items = parse_json(text, expect=list)
    out: list[T] = []
    for i, item in enumerate(raw_items):
        try:
            out.append(model_cls.model_validate(item))
        except ValidationError as e:
            logger.warning("第 %d 项不符合 %s 结构，已跳过: %s", i, model_cls.__name__, e)
    return out


def parse_model(text: str, model_cls: type[T]) -> T:
    data = parse_json(text, expect=dict)
    try:
        return model_cls.model_validate(data)
    except ValidationError as e:
        raise LlmOutputError(f"LLM 输出不符合 {model_cls.__name__} 结构: {e}") from e
