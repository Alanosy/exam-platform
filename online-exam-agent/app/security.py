"""安全上下文：解析与考试系统共享的 JWT(HS256)，识别用户身份与角色。

Token claim 结构（与考试系统一致）：
  - userInfo: JSON 字符串，包含 id、roleId(1学生/2教师/3管理员)、gradeId 等
  - authList: List[str]，如 ["role_student"]
"""
import json
from dataclasses import dataclass
from typing import Optional

import jwt
from fastapi import Header

from app.config import settings


@dataclass
class CurrentUser:
    user_id: Optional[int]
    role_code: Optional[int]  # 1学生 / 2教师 / 3管理员

    @property
    def role_name(self) -> Optional[str]:
        return {1: "student", 2: "teacher", 3: "admin"}.get(self.role_code) if self.role_code else None

    def is_teacher_or_admin(self) -> bool:
        return self.role_code in (2, 3)


def _decode(token: str) -> dict:
    """校验并解析 token，失败返回空 dict。"""
    try:
        return jwt.decode(token, settings.jwt_secret, algorithms=["HS256"])
    except Exception:
        return {}


def _from_claims(claims: dict) -> CurrentUser:
    user_info = claims.get("userInfo")
    if isinstance(user_info, str):
        try:
            user_info = json.loads(user_info)
        except Exception:
            user_info = {}
    if not isinstance(user_info, dict):
        user_info = {}
    return CurrentUser(
        user_id=user_info.get("id"),
        role_code=user_info.get("roleId"),
    )


def require_user(authorization: Optional[str] = Header(default=None)) -> CurrentUser:
    """FastAPI 依赖：解析 Authorization 头，返回当前用户。未携带则 user_id 为 None。"""
    if not authorization:
        return CurrentUser(user_id=None, role_code=None)
    token = authorization[7:] if authorization.startswith("Bearer ") else authorization
    return _from_claims(_decode(token))


def require_student(authorization: Optional[str] = Header(default=None)) -> CurrentUser:
    """FastAPI 依赖：要求学生身份。"""
    user = require_user(authorization)
    if user.user_id is None:
        from app.schemas.common import ApiError
        raise ApiError("未登录或登录已过期")
    return user


def require_teacher_or_admin(authorization: Optional[str] = Header(default=None)) -> CurrentUser:
    """FastAPI 依赖：要求教师/管理员身份。"""
    user = require_user(authorization)
    if not user.is_teacher_or_admin():
        from app.schemas.common import ApiError
        raise ApiError("仅教师/管理员可使用智能出题")
    return user
