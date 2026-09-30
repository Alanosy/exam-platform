"""统一响应结构（与考试系统 Result 保持一致：code=1 成功，code=0 失败）。

泛型 Result 用 typing.Generic 实现，FastAPI 会自动序列化。
"""
from typing import Any, Generic, Optional, TypeVar

from fastapi import HTTPException
from pydantic import BaseModel

T = TypeVar("T")


class Result(BaseModel, Generic[T]):
    code: int
    data: Optional[T] = None
    msg: str = ""

    @staticmethod
    def success(data: Any = None, msg: str = "成功") -> "Result[Any]":
        return Result(code=1, data=data, msg=msg)

    @staticmethod
    def failed(msg: str = "失败") -> "Result[Any]":
        return Result(code=0, data=None, msg=msg)


class ApiError(HTTPException):
    """业务异常：抛出后被全局异常处理器转为 Result.failed。"""

    def __init__(self, msg: str, code: int = 0):
        super().__init__(status_code=200, detail=msg)
        self.msg = msg
        self.code = code
