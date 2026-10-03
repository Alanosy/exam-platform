"""对话式 Agent（Chat）

与 /agent/plan/*（预置计划）不同，这里处理的是**自然语言指令**：

    「帮我创建 10 道关于计算机基础知识的题到计算机题库」

引擎负责把它拆成「意图 -> 槽位 -> 工具/技能调用」，
缺信息就中断问用户，写库前必须让人确认，每一步都留 Trace 给前端渲染。
"""

from app.chat.engine import chat
from app.chat.session import store as session_store

__all__ = ["chat", "session_store"]
