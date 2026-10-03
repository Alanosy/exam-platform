"""全局配置

全部从环境变量 / .env 读取（前缀 AGENT_），便于容器化与 Nacos 注入。

分层对应架构文档：
    L1 模型供应商  -> llm_*  + gateway.model_source
    L2 模型网关    -> gateway_*
    L3 能力层      -> tools_* / skills_* / memory_* / rag_*
    L4 编排层      -> agent_*
    L5 接入层      -> nacos_* / host / port
"""

from __future__ import annotations

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_prefix="AGENT_", extra="ignore")

    # ---------------- L5 接入层 ----------------
    host: str = "0.0.0.0"
    port: int = 9221
    debug: bool = False
    service_name: str = "ruoyi-exam-agent"

    # Nacos 服务注册
    nacos_enabled: bool = True
    nacos_server: str = "127.0.0.1:8848"
    nacos_namespace: str = "dev"
    nacos_group: str = "DEFAULT_GROUP"
    nacos_username: str = "nacos"
    nacos_password: str = "nacos"
    nacos_cluster: str = "DEFAULT"
    nacos_heartbeat_interval: int = 5
    # 注册用的 IP。留空则自动探测出口网卡地址；
    # 多网卡 / 容器 / 端口映射场景必须显式指定，否则别的服务会拿到不可达的地址
    nacos_ip: str = ""

    # ---------------- L2 模型网关 ----------------
    # 配置源优先级：mysql > env。任一源为空或失败都会自动降级到下一个。
    model_source: str = "mysql,env"  # 逗号分隔，按序尝试
    model_cache_ttl: int = 60        # 配置缓存秒数

    # 主备切换 / 熔断
    model_failover_enabled: bool = True
    model_max_retry: int = 1         # 单个模型内的重试次数（不含首次）
    model_fuse_threshold: int = 3    # 连续失败多少次后熔断
    model_fuse_cooldown: int = 60    # 熔断冷却秒数
    model_default_timeout: int = 60  # 单次调用超时（秒），可被模型配置覆盖

    # MySQL 只读（读取 ry-cloud.ai_model_config，绝不写入）
    mysql_enabled: bool = True
    mysql_host: str = "127.0.0.1"
    mysql_port: int = 3306
    mysql_user: str = "root"
    mysql_password: str = ""
    mysql_database: str = "ry-cloud"

    # ---------------- L1 模型供应商（env 兜底源）----------------
    # 支持多组：AGENT_LLM_MODELS__0__NAME=...（见 .env.example）
    llm_provider: str = "openai"
    llm_base_url: str = "https://api.deepseek.com/v1"
    llm_api_key: str = ""
    llm_model: str = "deepseek-chat"
    llm_temperature: float = 0.2
    llm_max_tokens: int = 2048
    llm_timeout: int = 60
    # 自测开关：不配真实 Key 时也能跑通全链路（网关->解析->返回），
    # 返回的是空结构，**仅供联调，严禁用于生产**
    llm_mock: bool = False

    # 多模型主备：JSON 数组字符串，形如
    # [{"name":"DeepSeek","model_name":"deepseek-chat","base_url":"...","api_key":"sk-xxx","priority":10},
    #  {"name":"Qwen","model_name":"qwen-plus","base_url":"...","api_key":"sk-yyy","priority":20}]
    llm_models: str = ""

    # ---------------- L3 工具层（回调 Java 网关）----------------
    tools_enabled: bool = True
    java_gateway_base_url: str = "http://127.0.0.1:9220"
    java_gateway_token: str = ""
    java_gateway_timeout: int = 10

    # ---------------- L3 记忆层 ----------------
    memory_backend: str = "auto"   # auto / redis / memory
    memory_ttl: int = 1800         # 短期记忆 TTL（秒）
    redis_host: str = "127.0.0.1"
    redis_port: int = 6379
    redis_password: str = ""
    redis_db: int = 0

    # ---------------- L3 知识层（RAG）----------------
    rag_enabled: bool = False
    rag_top_k: int = 4
    rag_vector_store: str = "chroma"
    rag_embedding_model: str = "bge-m3"

    # ---------------- L4 编排层 ----------------
    agent_max_steps: int = 12       # ReAct 最大步数
    agent_max_parallel: int = 4     # Multi-Agent 最大并行度
    agent_reflection_enabled: bool = True   # 是否启用 Critic 自检
    agent_reflection_min_score: float = 0.6 # Critic 打分低于此值触发重写
    agent_default_planner: str = "rule"     # rule / llm

    # ---------------- Skill / Prompt ----------------
    prompt_dir: str = "prompts"
    skill_dir: str = "app/skills"

    # ---------------- 观测 ----------------
    log_level: str = "INFO"
    call_log_enabled: bool = True
    call_log_size: int = 500        # 内存环形日志保留条数


settings = Settings()
