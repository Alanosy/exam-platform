"""L2 模型网关层

对外只有一个入口 AiModelRouter（app.gateway.router.router），
业务代码永远不直接接触 api_key 与供应商差异。
"""
