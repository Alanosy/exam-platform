"""统一返回体 R<T>

与 Java 侧 org.dromara.common.core.domain.R 对齐，字段保持一致，
Java 网关反序列化后可直接透传给前端。

    { "code": 200, "msg": "success", "data": {...} }

约定：
- code = 200 表示成功，其余为业务/系统错误
- msg  面向人，可安全展示
- data 永远存在（失败时为 null）
"""

from __future__ import annotations

from typing import Any, Generic, TypeVar

from fastapi import status
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field

T = TypeVar("T")

CODE_OK = 200
CODE_BAD_REQUEST = 400
CODE_UNAUTHORIZED = 401
CODE_FORBIDDEN = 403
CODE_NOT_FOUND = 404
CODE_ERROR = 500
CODE_AI_UNAVAILABLE = 503


class R(BaseModel, Generic[T]):
    """统一返回体"""

    code: int = Field(default=CODE_OK, description="状态码")
    msg: str = Field(default="success", description="提示信息")
    data: T | None = Field(default=None, description="业务数据")

    @classmethod
    def ok(cls, data: Any = None, msg: str = "success") -> "R[Any]":
        return cls(code=CODE_OK, msg=msg, data=data)

    @classmethod
    def fail(cls, msg: str, code: int = CODE_ERROR, data: Any = None) -> "R[Any]":
        return cls(code=code, msg=msg, data=data)


def ok(data: Any = None, msg: str = "success") -> dict[str, Any]:
    """返回可直接序列化的 dict。

    为什么不返回 R 对象：接口函数标注的是 `-> dict`，FastAPI 会拿返回值去校验
    response_model，pydantic 不接受 BaseModel 当 dict，会直接 500。
    R 类保留作契约声明与 Java 侧字段对齐用。
    """
    return {"code": CODE_OK, "msg": msg, "data": data}


def fail(msg: str, code: int = CODE_ERROR, data: Any = None) -> dict[str, Any]:
    return {"code": code, "msg": msg, "data": data}


def json_ok(data: Any = None, msg: str = "success", status_code: int = status.HTTP_200_OK) -> JSONResponse:
    """返回 JSONResponse，HTTP 状态码仍为 200（与 RuoYi 一致，错误靠 code 区分）"""
    return JSONResponse(
        status_code=status_code,
        content={"code": CODE_OK, "msg": msg, "data": data},
    )


def json_fail(
    msg: str,
    code: int = CODE_ERROR,
    data: Any = None,
    status_code: int = status.HTTP_200_OK,
) -> JSONResponse:
    return JSONResponse(
        status_code=status_code,
        content={"code": code, "msg": msg, "data": data},
    )
