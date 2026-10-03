"""启动自检脚本

用途：一键验证服务是否真的跑通（在没有交互式终端时尤其方便）。
它会打本机 9221 的接口，把结果写到 selftest-report.txt。

    .venv/bin/python selftest.py
"""

from __future__ import annotations

import json
import sys
import time
import urllib.request
from urllib.error import URLError

BASE = "http://127.0.0.1:9221"
REPORT = "selftest-report.txt"

CHECKS = [
    ("GET", "/health", None),
    ("GET", "/api/ai/skill/list", None),
    ("GET", "/api/ai/tool/list", None),
    ("GET", "/api/ai/model/list", None),
    ("GET", "/api/ai/agent/plan/list", None),
    (
        "POST",
        "/api/ai/skill/mark_score/run",
        {
            "input": {
                "stem": "简述 TCP 三次握手",
                "full_score": 10,
                "standard_answer": "SYN -> SYN-ACK -> ACK",
                "answer_text": "客户端发 SYN，服务端回 SYN-ACK，客户端再发 ACK",
            }
        },
    ),
]


def request(method: str, path: str, body: dict | None, timeout: int = 30):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(
        BASE + path, data=data, method=method, headers={"Content-Type": "application/json"}
    )
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return resp.status, json.loads(resp.read().decode())


def wait_up(timeout: int = 40) -> bool:
    deadline = time.time() + timeout
    while time.time() < deadline:
        try:
            request("GET", "/ping", None, timeout=3)
            return True
        except Exception:
            time.sleep(1)
    return False


def main() -> int:
    lines: list[str] = []
    ok_all = True

    if not wait_up():
        lines.append("FAIL: 服务未在 40s 内就绪，请确认 9221 端口已启动")
        _write(lines)
        return 1

    for method, path, body in CHECKS:
        try:
            status, data = request(method, path, body)
            payload = data.get("data") if isinstance(data, dict) else None
            if path == "/api/ai/skill/list":
                summary = f"skills={len(payload.get('items', [])) if payload else 0}"
            elif path == "/api/ai/tool/list":
                summary = f"tools={payload.get('count') if payload else 0}"
            elif path == "/api/ai/model/list":
                summary = f"models={payload.get('count') if payload else 0}"
            elif path == "/api/ai/agent/plan/list":
                summary = f"plans={len(payload.get('items', [])) if payload else 0}"
            elif path == "/health":
                summary = (
                    f"status={data.get('status')} "
                    f"nacos_registered={data.get('nacos', {}).get('registered')} "
                    f"skills={data.get('skills')} prompts={data.get('prompts')}"
                )
            else:
                summary = f"skill_ok={data.get('code') == 200}"
            lines.append(f"PASS {method} {path} -> HTTP {status} | {summary}")
            raw = json.dumps(data, ensure_ascii=False)[:600]
            lines.append(f"     raw: {raw}")
        except URLError as e:
            ok_all = False
            lines.append(f"FAIL {method} {path} -> {e}")
        except Exception as e:  # noqa: BLE001
            ok_all = False
            lines.append(f"FAIL {method} {path} -> {type(e).__name__}: {e}")

    lines.append("")
    lines.append("RESULT: " + ("ALL PASS" if ok_all else "HAS FAILURE"))
    _write(lines)
    return 0 if ok_all else 1


def _write(lines: list[str]) -> None:
    with open(REPORT, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")


if __name__ == "__main__":
    sys.exit(main())
