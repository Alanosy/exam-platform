"""业务流程（Agent 的「手」）

每个 flow 是一个 async 函数，签名统一：

    async def flow_xxx(slots, ctx, trace) -> (reply_md | None, ask | None, data)

约定：
- 返回 ask 表示**中断**，reply 为 None，engine 会把 slots 存进会话等用户回答；
- 返回 reply 表示本轮结束；
- 每个关键动作都要往 trace 里塞一个 TraceStep，前端据此渲染运行流程。

幂等性很重要：用户回答后 engine 会**从头重跑**整个 flow，
所以已经确定的槽位（bank_id / exam_id）必须存在 slots 里，
重跑时直接跳过对应的匹配步骤，不能重复弹同一个问题。
"""

from __future__ import annotations

import json
import logging
from typing import Any

from app.chat.heuristic import heuristic_plan
from app.chat.models import AskField, AskForm, TraceStep
from app.chat.planner import (
    Plan,
    PlanStep,
    fallback_summary_md,
    identity_audience,
    make_plan,
    manifest_text_of,
    plan_md,
    run_plan,
)
from app.chat.runtime import call_llm, call_skill, call_tool
from app.config import settings
from app.skills.base import SkillContext

logger = logging.getLogger(__name__)

TYPE_LABEL = {
    "SINGLE": "单选", "MULTIPLE": "多选", "JUDGE": "判断", "BLANK": "填空",
    "MATCH": "匹配", "SHORT_ANSWER": "简答", "ESSAY": "论述", "CODE": "编程",
    "UPLOAD_FILE": "文件上传",
}
DIFF_LABEL = {"easy": "简单", "medium": "中等", "hard": "困难"}
STATUS_LABEL = {"0": "草稿", "1": "启用", "2": "废弃"}

TYPE_OPTIONS = [{"label": v, "value": k} for k, v in TYPE_LABEL.items()]
DIFF_OPTIONS = [{"label": v, "value": k} for k, v in DIFF_LABEL.items()]
STATUS_OPTIONS = [{"label": v, "value": k} for k, v in STATUS_LABEL.items()]


def _think(trace: list[TraceStep], title: str, detail: str = "") -> None:
    trace.append(TraceStep(type="think", title=title, detail=detail, status="ok"))


def _preview_lines(items: list[dict[str, Any]], fmt) -> list[str]:
    return [fmt(i) for i in items[:8]]


# 题库下拉里「新建题库」这一项的值。用不可能与雪花 ID 撞车的常量，
# 解析时据此走建库分支。
NEW_BANK = "__new__"


async def _lookup_banks(
    slots: dict[str, Any], ctx: SkillContext, trace: list[TraceStep], topic: str
) -> tuple[list[dict[str, Any]], str, bool, bool]:
    """按候选关键词找题库

    返回 (候选题库, 命中关键词, 工具是否失败, 是否由关键词命中)。

    候选顺序：手填的库名 > 完整主题 > 派生的短关键词。
    完整主题先试是因为「TCP」这种短主题一旦被截短就再也匹配不上了。

    全都落空时再拉一次全量：让用户能直接从已有题库里挑一个，
    省掉「先选『从全部题库挑一个』再选一次」这多余的一轮。
    """
    candidates: list[str] = []
    for raw in (slots.get("bank_name"), topic, slots.get("bank_keyword")):
        value = str(raw or "").strip()
        if value and value not in candidates:
            candidates.append(value)

    keyword = candidates[0] if candidates else topic
    for candidate in candidates:
        data, step = await call_tool(
            "list_question_banks",
            {"keyword": candidate, "limit": 20},
            tenant_id=ctx.tenant_id,
            title=f"查找题库 · 关键词「{candidate}」",
        )
        items: list[dict[str, Any]] = (data or {}).get("items", []) if isinstance(data, dict) else []
        step.preview = _preview_lines(
            items, lambda b: f"{b.get('name')}（{b.get('questionCount', 0)} 题）"
        ) or (["工具调用失败，未能读取题库列表"] if data is None else [f"关键词「{candidate}」没有匹配到题库"])
        trace.append(step)
        if data is None:
            return [], candidate, True, False
        if items:
            return items, candidate, False, True

    data, step = await call_tool(
        "list_question_banks", {"keyword": "", "limit": 50}, tenant_id=ctx.tenant_id, title="拉取全部题库兜底"
    )
    if data is None:
        # 连主题都没有时关键词循环一次都不会进，兜底这次是唯一的探针，
        # 它失败就必须如实报「读不到题库」，不能当成「库里没有」
        return [], keyword, True, False
    items = (data or {}).get("items", []) if isinstance(data, dict) else []
    step.preview = _preview_lines(items, lambda b: f"{b.get('name')}（{b.get('questionCount', 0)} 题）")
    trace.append(step)
    # 全量命中的不算「关键词命中」：库里只有一个「测试题库」时不能拿它当自动匹配结果
    return items, keyword, False, False


def _missing_desc(slots: dict[str, Any], topic: str, count: int) -> str:
    """缺参数卡片上的说明：先告诉用户已经认出了什么，再让他补剩下的"""
    known: list[str] = []
    if topic:
        known.append(f"主题「{topic}」")
    if count:
        known.append(f"{count} 道")
    if str(slots.get("bank_name") or "").strip():
        known.append(f"题库「{slots['bank_name']}」")
    head = f"已识别到 {'、'.join(known)}。" if known else ""
    return head + "补齐下面几项后立刻开始生成，确认入库前还会给你看一遍题目。"


# ---------------------------------------------------------------- 出题入库

async def flow_question_create(
    slots: dict[str, Any], ctx: SkillContext, trace: list[TraceStep]
) -> tuple[str | None, AskForm | None, dict[str, Any]]:
    """出题 -> 选库 -> 生成 -> 确认 -> 入库

    两条铁律：
    1. **缺什么一次问完**。分多轮追问时，用户第二次填完很容易被弹回同一张卡
       （旧实现里「新建题库」分支在 return 之后，根本走不到），看起来就是「做不了」。
    2. 题库定位结果必须写回 slots。中断续跑会重跑整个 flow，
       不记住就得再查一遍，甚至再建一次同名库。
    """
    topic = str(slots.get("topic") or "").strip()
    try:
        count = int(slots.get("count") or 0)
    except (TypeError, ValueError):
        count = 0

    _think(trace, "解析出题需求", f"主题：{topic or '待补充'}，数量：{count or '待补充'}")

    # ---------- 1. 收集还差什么（只卡真正缺了就没法干的字段） ----------
    # 题型 / 难度给默认值先往下走，用户可以在入库前的确认卡上改（改了会重新生成）。
    missing: list[AskField] = []
    if not count:
        missing.append(AskField(key="count", label="题目数量", type="number", value=5, placeholder="1-30"))
    if not topic:
        missing.append(AskField(key="topic", label="出题主题 / 知识点", type="text", placeholder="如：计算机基础、网络协议"))

    # ---------- 2. 定位题库 ----------
    bank_id = str(slots.get("bank_id") or "")
    bank_name = str(slots.get("bank_name") or "")

    if bank_id == NEW_BANK:
        # 上一轮用户在表单里选了「新建题库」：这一轮直接建。
        # 千万别再查一遍题库——查不到就又弹同一张卡，等于死循环。
        new_name = str(slots.get("new_bank_name") or slots.get("bank_keyword") or topic or "新建题库").strip()
        data, step = await call_tool(
            "create_question_bank", {"bankName": new_name}, tenant_id=ctx.tenant_id, title=f"新建题库「{new_name}」"
        )
        trace.append(step)
        if data and data.get("id"):
            bank_id = str(data.get("id"))
            bank_name = str(data.get("name") or new_name)
            slots["bank_id"] = bank_id
            slots["bank_name"] = bank_name
            _think(trace, f"已新建题库「{bank_name}」", f"题库 ID {bank_id}")
        else:
            # 建库失败：清掉选择，退回让用户填已有题库的名字，避免卡死在同一张卡
            slots.pop("bank_id", None)
            bank_id = ""
            missing.append(
                AskField(
                    key="bank_name", label="题库名称", type="text", value=new_name,
                    placeholder="新建没成功，填一个已有题库的名字，我按名字再查一次",
                )
            )
    elif not bank_id:
        banks, keyword, tool_failed, by_keyword = await _lookup_banks(slots, ctx, trace, topic)
        if tool_failed:
            # 绝不问用户要 ID：那是雪花主键，用户不可能知道，也记不住。
            # 先问「题库叫什么」，下一轮拿这个名字再查一次；还是不通就如实收尾。
            manual_name = str(slots.get("bank_name") or "").strip()
            if manual_name:
                return (
                    f"题库服务仍然没有响应，没能定位到「{manual_name}」。\n\n"
                    "名字我已经记下了，等服务恢复后你再说一次「出题」就能接着往下走。",
                    None,
                    {},
                )
            missing.append(
                AskField(
                    key="bank_name", label="题库名称", type="text", value=keyword,
                    placeholder="如：计算机基础题库",
                    tip="暂时读不到题库列表，填名称我按名字再查一次",
                )
            )
        elif banks and by_keyword and len(banks) == 1:
            bank_id = str(banks[0].get("id"))
            bank_name = str(banks[0].get("name"))
            slots["bank_id"] = bank_id
            slots["bank_name"] = bank_name
            _think(trace, f"自动匹配到唯一题库「{bank_name}」", f"题库 ID {bank_id}，命中规则：关键词「{keyword}」唯一匹配")
        else:
            # 命中多个 / 一个都没命中：把候选（含全量兜底）连同「新建」一起摆出来，
            # 一张卡解决，用户不必先选「挑一个」再选一次
            options = [
                {"label": f"{b.get('name')}（{b.get('questionCount', 0)} 题）", "value": str(b.get("id"))}
                for b in banks
            ]
            options.append({"label": f"新建题库「{keyword or topic}」", "value": NEW_BANK})
            missing.append(
                AskField(
                    key="bank_id",
                    label="写到哪个题库？" + ("没有完全匹配的，可以从已有题库里挑" if banks else "当前没有可用题库"),
                    type="select",
                    options=options,
                    # 关键词确实命中了才默认选第一个；只是「全量兜底」出来的库
                    # 跟主题没关系，默认停在「新建」，别让人一回车就写错库
                    value=str(banks[0].get("id")) if (banks and by_keyword) else NEW_BANK,
                )
            )
            missing.append(
                AskField(
                    key="new_bank_name", label="新建题库名称", type="text",
                    value=str(slots.get("new_bank_name") or keyword or topic),
                    required=False, tip="选了「新建题库」时才生效",
                )
            )

    if missing:
        # 一次把缺的都问完，别让用户一轮一轮猜
        return None, AskForm(
            kind="form",
            title="补齐这些信息就开始出题",
            desc=_missing_desc(slots, topic, count),
            fields=missing,
            submit_text="开始生成",
        ), {}

    if not bank_name:
        data, _ = await call_tool("list_question_banks", {"keyword": "", "limit": 50}, tenant_id=ctx.tenant_id)
        banks = (data or {}).get("items", []) if isinstance(data, dict) else []
        bank_name = next((str(b.get("name")) for b in banks if str(b.get("id")) == bank_id), f"题库 {bank_id}")

    # ---------- 3. 生成题目 ----------
    question_type = str(slots.get("question_type") or "SINGLE")
    difficulty = str(slots.get("difficulty") or "medium")
    gen_payload = {
        "question_type": question_type,
        "difficulty": difficulty,
        "knowledge_points": [topic],
        "count": count,
        "score": float(slots.get("score") or 5),
        "rag_context": "",
        "extra": {"topic": topic},
    }
    # 用户在确认卡上改了题型 / 难度才重新生成，否则复用上一轮结果，
    # 避免「点个确认还要再等一次模型」这种毫无意义的等待
    gen_key = f"{question_type}|{difficulty}|{count}|{topic}"
    questions = slots.get("__questions") if slots.get("__gen_key") == gen_key else None
    if questions:
        # 复用时也要给 step 赋值：下面统一往 step 上写预览，不赋值会踩 UnboundLocalError
        step = TraceStep(type="skill", ref="question_gen", status="ok", title="复用上一轮已生成的题目")
        _think(trace, "复用上一轮已生成的题目", "生成参数未变化，跳过重复调用模型")
    else:
        questions, step = await call_skill(
            "question_gen", gen_payload, ctx,
            title=f"生成 {count} 道{TYPE_LABEL.get(question_type, '')}题",
        )
        trace.append(step)
        slots["__questions"] = questions or []
        slots["__gen_key"] = gen_key

    if not questions:
        hint = "（当前是 MOCK 模式：没有配置真实模型密钥，模型返回空结构）" if settings.llm_mock else ""
        return (
            f"题目生成失败：模型没有返回可用结果。{hint}\n\n"
            "请检查 `ai_model_config` 里的模型配置（base_url / api_key / 模型名），改完重试。",
            None,
            {},
        )

    questions = [q for q in questions if isinstance(q, dict) and q.get("stem")]
    step.preview = [f"{i + 1}. {q.get('stem', '')[:40]}" for i, q in enumerate(questions[:5])]

    # ---------- 4. 入库前确认（写操作必须让人点头） ----------
    if not slots.get("confirmed"):
        preview_md = _questions_preview_md(questions, limit=5)
        return None, AskForm(
            kind="confirm",
            title=f"已生成 {len(questions)} 道题，确认写入题库「{bank_name}」吗？",
            desc=preview_md,
            fields=[
                AskField(key="question_type", label="题型", type="select", options=TYPE_OPTIONS, value=question_type, required=False, tip="改了会重新生成"),
                AskField(key="difficulty", label="难度", type="select", options=DIFF_OPTIONS, value=difficulty, required=False, tip="改了会重新生成"),
                AskField(key="status", label="入库状态", type="select", options=STATUS_OPTIONS, value=str(slots.get("status") or "0"), required=False),
            ],
            submit_text="确认入库",
            cancel_text="算了",
        ), {"questions": questions, "bankId": bank_id, "bankName": bank_name}

    # ---------- 5. 入库 ----------
    save_payload = {
        "bankId": bank_id,
        "status": str(slots.get("status") or "0"),
        # 试题挂在出题人名下：question.create_user 是 NOT NULL，
        # 服务端只在拿不到时才兜底成管理员
        "createUser": _as_int(ctx.user_id),
        "questions": [_to_save_item(q) for q in questions],
    }
    data, step = await call_tool(
        "save_questions", save_payload, tenant_id=ctx.tenant_id, title=f"写入 {len(questions)} 道题到「{bank_name}」"
    )
    trace.append(step)

    # Java 侧 saveQuestions 直接返回 List<Long>（JSON 数组），
    # 兼容两种返回：{"ids":[...]} 或 [...]
    if isinstance(data, dict):
        saved_ids = data.get("ids") or []
    elif isinstance(data, list):
        saved_ids = data
    else:
        saved_ids = []

    if not saved_ids:
        # 一条都没写进去时绝不能报「已完成」：服务端返回 200 但 ids 为空，
        # 通常是试题表必填字段（如 create_user）缺失，逐题入库被服务端吞掉了异常。
        step.status = "error"
        step.detail = f"{step.detail}\n服务端返回 0 条：请查看题库服务日志（常见原因：必填字段为空）"
        return (
            f"题目已生成，但**一条都没写进题库「{bank_name}」**（服务端返回 0 条）。\n\n"
            "生成结果没有丢，确认题库服务日志里的入库报错后再让我重跑一次。\n\n"
            + _questions_preview_md(questions, limit=10),
            None,
            {"questions": questions, "bankId": bank_id},
        )

    step.preview = [f"新增试题 ID：{', '.join(str(i) for i in saved_ids[:10])}"] if saved_ids else []
    if len(saved_ids) < len(questions):
        step.status = "error"
        step.detail = f"{step.detail}\n{len(questions) - len(saved_ids)} 道入库失败，见题库服务日志"
    trace.append(TraceStep(type="done", title="入库完成", detail=f"共写入 {len(saved_ids)} 道题", status="ok"))

    return (
        _questions_result_md(questions, bank_name, bank_id, saved_ids, str(slots.get("status") or "0")),
        None,
        {"questions": questions, "bankId": bank_id, "ids": saved_ids},
    )


def _as_int(value: Any) -> int | None:
    """用户 ID 转整数；拿不到就返回 None，让服务端兜底"""
    try:
        return int(str(value).strip())
    except (TypeError, ValueError):
        return None


def _to_save_item(q: dict[str, Any]) -> dict[str, Any]:
    """Skill 输出 -> 入库工具入参

    答案字段在 Skill 里是字符串形式的 JSON，原样传给 Java 由它落库；
    选项统一成 {key, content}。
    """
    options = q.get("options") or []
    return {
        "questionType": q.get("question_type") or "SINGLE",
        "stem": q.get("stem") or "",
        "analysis": q.get("analysis") or "",
        "answer": q.get("answer") or "",
        "difficulty": q.get("difficulty") or "medium",
        "score": q.get("score") or 5,
        "options": [
            {"key": o.get("key"), "content": o.get("content")} for o in options if isinstance(o, dict)
        ],
        "knowledgePoints": q.get("knowledge_points") or [],
    }


def _questions_preview_md(questions: list[dict[str, Any]], limit: int = 5) -> str:
    lines = ["| # | 题型 | 题干 | 答案 |", "| :-- | :-- | :-- | :-- |"]
    for i, q in enumerate(questions[:limit], 1):
        stem = str(q.get("stem", "")).replace("\n", " ")[:36]
        answer = _answer_text(q.get("answer"))
        lines.append(
            f"| {i} | {TYPE_LABEL.get(q.get('question_type', ''), q.get('question_type', ''))} | {stem} | {answer} |"
        )
    if len(questions) > limit:
        lines.append(f"\n（还有 {len(questions) - limit} 道，入库后到试题列表查看）")
    return "\n".join(lines)


def _answer_text(answer: Any) -> str:
    """答案字段展示：优先解析成中文可读形式"""
    if not answer:
        return "-"
    if not isinstance(answer, str):
        return str(answer)
    try:
        obj = json.loads(answer)
    except (ValueError, TypeError):
        return answer[:20]
    if isinstance(obj, dict):
        keys = obj.get("rightKeys")
        if keys:
            return "".join(keys)
        if obj.get("answer"):
            return str(obj["answer"])[:20]
        if obj.get("blanks"):
            return "/".join(",".join(b.get("answers", [])) for b in obj["blanks"])[:20]
    return answer[:20]


def _questions_result_md(
    questions: list[dict[str, Any]], bank_name: str, bank_id: str, ids: list[Any], status: str
) -> str:
    lines = [
        f"已完成：向题库 **{bank_name}**（ID `{bank_id}`）写入 **{len(ids)}** 道题，"
        f"状态为 **{STATUS_LABEL.get(status, status)}**。",
        "",
        "| # | 题型 | 题干 | 答案 |",
        "| :-- | :-- | :-- | :-- |",
    ]
    for i, q in enumerate(questions, 1):
        stem = str(q.get("stem", "")).replace("\n", " ")[:40]
        lines.append(
            f"| {i} | {TYPE_LABEL.get(q.get('question_type', ''), q.get('question_type', ''))} "
            f"| {stem} | {_answer_text(q.get('answer'))} |"
        )
    lines += [
        "",
        "> 题目默认落在草稿态，建议到「试题管理」复核后再启用，避免 AI 生成的错题直接进卷。",
    ]
    return "\n".join(lines)


# ---------------------------------------------------------------- 答题情况分析

async def flow_exam_analysis(
    slots: dict[str, Any], ctx: SkillContext, trace: list[TraceStep]
) -> tuple[str | None, AskForm | None, dict[str, Any]]:
    """定位考试 -> 取答卷数据 -> 模型分析 -> Markdown 报告"""
    _think(trace, "解析查询意图", f"目标考试：{slots.get('exam_keyword') or '待补充'}")

    if not slots.get("exam_keyword") and not slots.get("exam_id"):
        return None, AskForm(
            kind="form",
            title="想看哪场考试的答题情况？",
            desc="给我一个考试名称关键词，我从考试列表里找。",
            fields=[AskField(key="exam_keyword", label="考试名称关键词", type="text", placeholder="如：期中、Java")],
            submit_text="查一下",
        ), {}

    exam_id = str(slots.get("exam_id") or "")
    exam_name = str(slots.get("exam_name") or "")

    if not exam_id:
        keyword = str(slots.get("exam_keyword"))
        data, step = await call_tool(
            "find_exam", {"keyword": keyword, "limit": 10}, tenant_id=ctx.tenant_id, title=f"查找考试 · 「{keyword}」"
        )
        exams: list[dict[str, Any]] = (data or {}).get("items", []) if isinstance(data, dict) else []
        step.preview = _preview_lines(exams, lambda e: f"{e.get('name')}（{e.get('statusLabel') or e.get('status')}）")
        trace.append(step)

        if data is None:
            # 同样不问 ID：用户只知道考试叫什么名字
            manual = str(slots.get("exam_keyword") or "").strip()
            if manual:
                return (
                    f"考试服务仍然没有响应，没能定位到「{manual}」。\n\n"
                    "等服务恢复后再问我一次就行。",
                    None,
                    {},
                )
            return None, AskForm(
                kind="form", title="暂时读不到考试列表",
                desc="考试服务没返回数据。告诉我想看哪场考试（填名称关键词），我拿它再查一次。",
                fields=[
                    AskField(key="exam_keyword", label="考试名称关键词", type="text", placeholder="如：期中、Java")
                ],
                submit_text="查一下",
            ), {}
        if not exams:
            return f"没有找到名称包含「{keyword}」的考试，换个关键词试试？", None, {}
        if len(exams) > 1:
            return None, AskForm(
                kind="form",
                title=f"找到 {len(exams)} 场相关考试，看哪一场？",
                fields=[
                    AskField(
                        key="exam_id", label="目标考试", type="select",
                        options=[{"label": str(e.get("name")), "value": str(e.get("id"))} for e in exams],
                        value=str(exams[0].get("id")),
                    )
                ],
                submit_text="查一下",
            ), {}
        exam_id = str(exams[0].get("id"))
        exam_name = str(exams[0].get("name"))
        _think(trace, f"自动定位到唯一考试「{exam_name}」", f"考试 ID {exam_id}")

    data, step = await call_tool(
        "exam_answer_stats", {"examId": exam_id}, tenant_id=ctx.tenant_id, title="拉取答卷与成绩数据"
    )
    trace.append(step)
    if not data:
        return "没能拿到这场考试的答卷数据（答题服务无返回或服务未启动）。", None, {}
    stats = data
    exam_name = stats.get("examName") or exam_name or f"考试 {exam_id}"
    step.preview = [
        f"参考 {stats.get('total', 0)} 人 / 已交卷 {stats.get('submitted', 0)} 人",
        f"平均分 {stats.get('avgScore', 0)} / 及格率 {stats.get('passRate', 0)}",
    ]

    content, step = await call_llm(
        "chat_analysis",
        {
            "exam_name": exam_name,
            "stats_json": json.dumps(stats, ensure_ascii=False, indent=1),
            "question": slots.get("__question", "这场考试的答题情况怎么样？"),
        },
        ctx,
        title="分析答题数据并生成结论",
    )
    trace.append(step)

    if not content or content.strip() in ("{}", "[]"):
        content = _fallback_analysis_md(stats)

    head = _stats_md(exam_name, exam_id, stats)
    trace.append(TraceStep(type="done", title="分析完成", detail="数据已取回并生成结论", status="ok"))
    return f"{head}\n\n---\n\n{content}", None, {"stats": stats}


def _stats_md(exam_name: str, exam_id: str, stats: dict[str, Any]) -> str:
    lines = [
        f"### 「{exam_name}」答题概况（ID `{exam_id}`）",
        "",
        "| 指标 | 数值 |",
        "| :-- | --: |",
        f"| 参考人数 | {stats.get('total', 0)} |",
        f"| 已交卷 | {stats.get('submitted', 0)} |",
        f"| 平均分 | {stats.get('avgScore', 0)} |",
        f"| 最高分 / 最低分 | {stats.get('maxScore', 0)} / {stats.get('minScore', 0)} |",
        f"| 及格率 | {stats.get('passRate', 0)} |",
    ]
    bands = stats.get("scoreBands") or []
    if bands:
        lines += ["", "**分数段分布**", ""]
        lines.append("| 分数段 | 人数 |")
        lines.append("| :-- | --: |")
        for b in bands:
            lines.append(f"| {b.get('band')} | {b.get('count')} |")
    return "\n".join(lines)


def _fallback_analysis_md(stats: dict[str, Any]) -> str:
    """模型没返回内容时的规则化结论（保证对话永远有话说）"""
    total = int(stats.get("total") or 0)
    submitted = int(stats.get("submitted") or 0)
    if total == 0:
        return "这场考试还没有人提交答卷，暂时没有可分析的样本。"
    rate = submitted / total if total else 0
    points: list[str] = []
    points.append(f"- 交卷率 **{rate:.0%}**，未交卷 {total - submitted} 人。")
    avg = float(stats.get("avgScore") or 0)
    if avg >= 85:
        points.append(f"- 平均分 {avg}，整体掌握较好，可考虑提高区分度。")
    elif avg >= 60:
        points.append(f"- 平均分 {avg}，处于及格线附近，建议针对低分题做专项讲评。")
    else:
        points.append(f"- 平均分 {avg}，整体偏弱，建议核查试卷难度与教学覆盖面。")
    bands = stats.get("scoreBands") or []
    low = sum(int(b.get("count") or 0) for b in bands if str(b.get("band", "")).startswith(("0", "1", "2", "3", "4", "5")))
    if low:
        points.append(f"- 低分段（60 分以下）约 {low} 人，是优先辅导对象。")
    return "\n".join(points)


# ---------------------------------------------------------------- 检索试题

async def flow_question_search(
    slots: dict[str, Any], ctx: SkillContext, trace: list[TraceStep]
) -> tuple[str | None, AskForm | None, dict[str, Any]]:
    _think(trace, "解析检索意图", f"关键词：{slots.get('keyword') or '待补充'}")
    if not slots.get("keyword"):
        return None, AskForm(
            kind="form", title="想找什么题？",
            fields=[AskField(key="keyword", label="关键词", type="text", placeholder="如：TCP、二叉树")],
            submit_text="搜一下",
        ), {}

    data, step = await call_tool(
        "search_questions",
        {"keyword": slots["keyword"], "limit": int(slots.get("limit") or 20)},
        tenant_id=ctx.tenant_id,
        title=f"检索试题 · 「{slots['keyword']}」",
    )
    trace.append(step)
    if data is None:
        return "题库服务没有返回结果，检索失败。", None, {}
    items = data.get("items", []) if isinstance(data, dict) else []
    step.preview = _preview_lines(items, lambda q: str(q.get("title") or q.get("stem") or "")[:40])
    if not items:
        return f"没有检索到包含「{slots['keyword']}」的试题。", None, {}

    lines = [f"检索到 **{len(items)}** 道题：", "", "| ID | 题型 | 题干 | 难度 |", "| :-- | :-- | :-- | :-- |"]
    for q in items[:20]:
        title = str(q.get("title") or q.get("stem") or "").replace("\n", " ")[:40]
        lines.append(
            f"| {q.get('id')} | {TYPE_LABEL.get(q.get('questionType', ''), q.get('questionType', ''))} "
            f"| {title} | {DIFF_LABEL.get(q.get('difficulty', ''), q.get('difficulty', ''))} |"
        )
    trace.append(TraceStep(type="done", title="检索完成", status="ok"))
    return "\n".join(lines), None, {"items": items}


# ---------------------------------------------------------------- 通用：规划 -> 执行 -> 汇总

async def flow_general(
    slots: dict[str, Any], ctx: SkillContext, trace: list[TraceStep]
) -> tuple[str | None, AskForm | None, dict[str, Any]]:
    """没预设流程（或预设流程覆盖不到）时的通用路径

    先让模型规划：能做就拆步骤一步步跑，做完汇总成 Markdown；
    做不到就把「为什么做不到」讲清楚 —— 这比回一句「我听不懂」有用得多。
    """
    question = str(slots.get("__question") or "")
    history = str(slots.get("__history") or "")
    _think(trace, "理解需求", question[:60])

    # 只有在「用户刚确认过写操作」这一条路径上才复用上一轮计划；
    # 否则（用户取消了、或者换了个新问题）必须重新规划，不能拿旧计划硬套
    plan = _plan_from_slots(slots) if slots.get("confirmed") else None
    if plan is None:
        audience = identity_audience(ctx.extra.get("identity"))
        plan = await make_plan(question, ctx, trace, history=history)
        if plan is None:
            # 模型拿不到计划（MOCK / 输出不规范）时不能就这么装傻：
            # 退到规则规划器，覆盖住高频问法，保证「先规划再执行」仍然成立
            plan = heuristic_plan(question, audience=audience)
        if plan is None:
            _think(trace, "规划失败，退回直接回答", "模型与规则都没有给出可用计划")
            return await flow_chat(slots, ctx, trace)
        slots["__plan"] = plan.model_dump()
        _think(trace, f"规划完成：{plan.goal}", plan_md(plan))

    if not plan.feasible:
        return _infeasible_md(plan), None, {"plan": plan.model_dump()}

    # 写操作：先给确认卡，把计划摆出来让人点头
    if plan.has_write() and ctx.extra.get("confirm_write", True) and not slots.get("confirmed"):
        return None, AskForm(
            kind="confirm",
            title=f"要执行 {len(plan.steps)} 步，其中含写操作",
            desc=plan_md(plan) + "\n\n> 写操作会真实改动系统数据，确认后才执行。",
            submit_text="确认执行",
            cancel_text="算了",
        ), {"plan": plan.model_dump()}

    results = await run_plan(plan, ctx, trace)
    slots.pop("__plan", None)
    slots.pop("confirmed", None)

    steps_text = "\n".join(
        f"{r['index']}. {r['title']}（{r['ref']}）{'成功' if r['ok'] else '失败'}" for r in results
    ) or "（无步骤）"
    content, step = await call_llm(
        "chat_summarize",
        {
            "question": question,
            "steps": steps_text,
            "results": json.dumps(results, ensure_ascii=False, indent=1),
        },
        ctx,
        title="汇总执行结果",
    )
    trace.append(step)
    if not content or content.strip() in ("{}", "[]"):
        content = fallback_summary_md(question, plan, results)
    trace.append(TraceStep(type="done", title="执行完成", detail=f"共 {len(results)} 步", status="ok"))
    return content, None, {"plan": plan.model_dump(), "results": results}


async def _load_brief(
    slots: dict[str, Any], ctx: SkillContext, trace: list[TraceStep]
) -> tuple[str, str]:
    """取一次系统说明书 + 接口清单，结果缓存在槽位里（一轮里可能用好几次）"""
    if "__system_brief" in slots and "__manifest" in slots:
        return str(slots["__system_brief"]), str(slots["__manifest"])
    data, step = await call_tool(
        "api_manifest",
        {"userId": str(ctx.user_id or "")},
        tenant_id=ctx.tenant_id,
        title="读取系统说明书",
    )
    trace.append(step)
    brief = str((data or {}).get("systemBrief") or "") if isinstance(data, dict) else ""
    slots["__system_brief"] = brief
    slots["__manifest"] = manifest_text_of(data)
    return brief, str(slots["__manifest"])


def _plan_from_slots(slots: dict[str, Any]) -> Plan | None:
    """从槽位里还原上一轮规划好的计划"""
    raw = slots.get("__plan")
    if not isinstance(raw, dict):
        return None
    try:
        return Plan(
            goal=str(raw.get("goal") or ""),
            feasible=bool(raw.get("feasible", True)),
            reason=str(raw.get("reason") or ""),
            steps=[PlanStep(**s) for s in (raw.get("steps") or []) if isinstance(s, dict)],
            direct_answer=str(raw.get("direct_answer") or ""),
        )
    except Exception:  # noqa: BLE001
        logger.warning("还原计划失败，重新规划")
        return None


def _infeasible_md(plan: Plan) -> str:
    """做不了也要说清楚：原因 + 差什么 + 建议"""
    lines = [f"这件事我现在做不了：{plan.goal or '你的需求'}", ""]
    if plan.reason:
        lines += [f"**原因**：{plan.reason}", ""]
    lines += [
        "常见原因与出路：",
        "- 需要某个权限：让管理员给你的角色加上对应权限后，我就能做了",
        "- 需要具体对象（考试 / 题库 / 试卷）：告诉我名字，我帮你查出来再继续",
        "- 系统里确实没有这个能力：这就是纯人工环节，我帮不了",
    ]
    return "\n".join(lines)


# ---------------------------------------------------------------- 自由问答

async def flow_chat(
    slots: dict[str, Any], ctx: SkillContext, trace: list[TraceStep]
) -> tuple[str | None, AskForm | None, dict[str, Any]]:
    _think(trace, "理解问题", "未命中预置流程，交给模型直接回答")
    brief, manifest = await _load_brief(slots, ctx, trace)
    content, step = await call_llm(
        "chat_free",
        {
            "question": slots.get("__question", ""),
            "history": slots.get("__history", ""),
            "system_brief": brief,
            "manifest": manifest,
        },
        ctx,
        title="模型作答",
    )
    trace.append(step)
    if not content or content.strip() in ("{}", "[]"):
        return (
            "我没能生成回答。可能原因：\n\n"
            "1. 模型未配置或处于 MOCK 模式（`AGENT_LLM_MOCK=true`）；\n"
            "2. 模型服务不可达或密钥失效。\n\n"
            "配好模型后再试一次就行。你也可以直接下达具体指令，例如：\n"
            "- 「帮我创建 10 道关于计算机基础知识的题到计算机题库」\n"
            "- 「期中考试的答题情况怎么样」",
            None,
            {},
        )
    trace.append(TraceStep(type="done", title="回答完成", status="ok"))
    return content, None, {}


FLOWS = {
    "question_create": flow_question_create,
    "exam_analysis": flow_exam_analysis,
    "question_search": flow_question_search,
    # 开放域问题走规划器：能拆步骤就执行，拆不出来它会自己说清楚为什么做不到
    "chat": flow_general,
    "general": flow_general,
}
