"""L3 工具层

把考试系统的业务能力（经 Java 网关 REST 暴露）包装成 Agent 可调用的工具。
写操作工具必须声明 risk_level=WRITE，由 Guardrail 决定是否放行或转人工。
"""
