"""规则兜底规划器

为什么需要它：
    模型没配好（MOCK / 密钥失效 / 输出不规范）时，规划器拿不到计划，
    整个助手就退化成「模型直接回答」——也就是又变回了那个什么都不懂的聊天框。
    这里用一把关键字规则覆盖住**最高频的那十几句问法**，
    保证「先判断能不能做 → 拆步骤 → 逐步执行」这条链路永远成立。

它只做只读查询：规则规划出来的计划里不会出现写操作，
改数据这件事必须交给模型规划 + 用户确认，不能靠猜。

步骤参数支持 `{1.items.0.id}` 这种占位符（1 = 第几步的结果），
由 planner.run_step 在执行前替换，所以「按名字查考试 → 查它的统计」能串起来。
"""

from __future__ import annotations

import re
from typing import Any

from app.chat.planner import Plan, PlanStep

# 提问里这些词只是语气，不该被当成考试/题库的名字
STOPWORDS = (
    "的", "了", "吗", "呢", "请", "帮我", "我想", "我要", "查一下", "看一下", "统计", "分析",
    "情况", "怎么样", "如何", "多少", "几个", "哪些", "所有", "全部", "一下", "给我", "列出",
    "查询", "帮我看看", "有没有", "有没有人",
)


def _keyword(question: str, drop: tuple[str, ...] = ()) -> str:
    """从提问里抠出疑似的对象名称"""
    text = question.strip()
    for word in STOPWORDS + drop:
        text = text.replace(word, " ")
    text = re.sub(r"[，。？！,.?!、；;：:\"'`（）()\[\]【】]", " ", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text[:20]


def heuristic_plan(
    question: str,
    audience: str = "any",
    manifest_available: bool = True,
) -> Plan | None:
    """按关键字猜一个只读计划；猜不出来返回 None（交给上层自由问答）"""
    q = question.strip()
    if not q:
        return None

    # ---------------- 考生本人视角 ----------------
    if _any(q, ("错题", "错题库", "做错的题")):
        steps = [PlanStep(kind="api", ref="GET /practice/wrong/overview", title="看错题概览", params={})]
        if _any(q, ("列表", "有哪些", "全部", "看看")):
            steps.append(PlanStep(kind="api", ref="GET /practice/wrong/list", title="列出错题", params={}))
        return Plan(goal="查看我的错题", steps=steps)

    if _any(q, ("我的考试", "我要考", "我能考", "待考", "可参加的考试", "这学期有哪些考试")):
        return Plan(
            goal="查看我可以参加的考试",
            steps=[PlanStep(kind="api", ref="GET /answer/record/center", title="查我的考试", params={})],
        )

    if _any(q, ("我的成绩", "我考了多少", "我的答卷")) and audience != "admin":
        return Plan(
            goal="查看我的历史成绩",
            steps=[PlanStep(kind="api", ref="GET /answer/record/records", title="查我的答卷", params={})],
        )

    if _any(q, ("我的证书", "证书")) and audience == "student":
        return Plan(
            goal="查看我的证书",
            steps=[PlanStep(kind="api", ref="GET /cert/mine/list", title="查我的证书", params={})],
        )

    # ---------------- 管理 / 教师视角 ----------------
    if audience == "student":
        return None

    if _any(q, ("待阅", "阅卷", "还没阅", "阅卷进度")):
        return Plan(
            goal="查看还有多少没阅",
            steps=[
                PlanStep(kind="api", ref="GET /mark/exam/list", title="看哪些考试有待阅", params={}),
                PlanStep(kind="api", ref="GET /mark/task/list", title="看阅卷任务进度", params={}),
            ],
        )

    if _any(q, ("作弊", "监考", "切屏", "防作弊", "违纪")):
        keyword = _keyword(q, drop=("作弊", "监考", "切屏", "防作弊", "违纪", "事件", "记录"))
        steps = [PlanStep(kind="api", ref="GET /proctor/overview", title="看监考总览", params={})]
        if keyword:
            steps.insert(
                0,
                PlanStep(
                    kind="tool", ref="resolve_entity", title=f"定位考试「{keyword}」",
                    params={"kind": "exam", "keyword": keyword, "limit": 10},
                ),
            )
            steps.append(
                PlanStep(
                    kind="api", ref="GET /proctor/event/list", title="查这场考试的作弊事件",
                    params={"query": {"examId": "{1.items.0.id}"}},
                )
            )
        return Plan(goal="查看防作弊情况", steps=steps)

    if _any(q, ("题库", "试题库")):
        keyword = _keyword(q, drop=("题库", "试题库", "有哪些", "列表"))
        return Plan(
            goal="查看题库",
            steps=[
                PlanStep(
                    kind="api", ref="GET /question/bank/list", title="查题库列表",
                    params={"query": {"name": keyword}} if keyword else {},
                )
            ],
        )

    if _any(q, ("试卷",)) and _any(q, ("有哪些", "列表", "多少")):
        keyword = _keyword(q, drop=("试卷", "有哪些", "列表", "多少"))
        return Plan(
            goal="查看试卷",
            steps=[
                PlanStep(
                    kind="api", ref="GET /paper/list", title="查试卷列表",
                    params={"query": {"paperName": keyword}} if keyword else {},
                )
            ],
        )

    if _any(q, ("考试", "考卷")) and _any(
        q, ("答题情况", "考得", "成绩", "及格", "平均分", "分数段", "正确率", "分析", "统计", "名单", "交卷", "参考人数")
    ):
        keyword = _keyword(
            q,
            drop=("考试", "考卷", "这场", "这次", "本次", "答题", "成绩", "及格", "平均分",
                  "分数段", "正确率", "分析", "统计", "名单", "交卷", "参考人数"),
        )
        steps: list[PlanStep] = []
        if keyword:
            steps.append(
                PlanStep(
                    kind="tool", ref="resolve_entity", title=f"定位考试「{keyword}」",
                    params={"kind": "exam", "keyword": keyword, "limit": 10},
                )
            )
            steps.append(
                PlanStep(
                    kind="api", ref="GET /exam/{1.items.0.id}/situation/overview",
                    title="查这场考试的实时概况", params={},
                )
            )
            if _any(q, ("正确率", "哪道题", "题目")):
                steps.append(
                    PlanStep(
                        kind="api", ref="GET /stat/exam/{1.items.0.id}/questions",
                        title="查题目正确率", params={},
                    )
                )
            elif _any(q, ("名单", "谁", "考生")):
                steps.append(
                    PlanStep(
                        kind="api", ref="GET /exam/{1.items.0.id}/situation/records",
                        title="查考生名单与得分", params={},
                    )
                )
            return Plan(goal=f"分析「{keyword}」的答题情况", steps=steps)
        return Plan(
            goal="看看最近有哪些考试可以分析",
            steps=[PlanStep(kind="api", ref="GET /exam/list", title="列考试", params={})],
            reason="没听出具体是哪场考试，先把考试列出来给你选",
        )

    if _any(q, ("有哪些考试", "考试列表", "多少场考试", "最近的考试")):
        return Plan(
            goal="列出考试",
            steps=[PlanStep(kind="api", ref="GET /exam/list", title="查考试列表", params={})],
        )

    if _any(q, ("总览", "概览", "首页", "大盘")) and manifest_available:
        return Plan(
            goal="看系统总览",
            steps=[PlanStep(kind="api", ref="GET /stat/home/overview", title="查首页总览", params={})],
        )

    return None


def _any(text: str, words: tuple[str, ...]) -> bool:
    return any(w in text for w in words)


__all__ = ["heuristic_plan", "_keyword"]
