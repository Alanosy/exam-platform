"""配置项，全部从环境变量读取，便于容器化与 Nacos 注入"""

from __future__ import annotations

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_prefix="AGENT_", extra="ignore")

    # 服务
    host: str = "0.0.0.0"
    port: int = 9221
    debug: bool = False

    # Nacos
    nacos_enabled: bool = True
    nacos_server: str = "127.0.0.1:8848"
    nacos_namespace: str = "dev"
    nacos_group: str = "DEFAULT_GROUP"
    nacos_username: str = ""
    nacos_password: str = ""

    # LLM（默认通义千问兼容 OpenAI 协议，可切 DeepSeek / OpenAI / vLLM）
    llm_provider: str = "openai"
    llm_base_url: str = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    llm_api_key: str = ""
    llm_model: str = "qwen-plus"
    llm_temperature: float = 0.2
    llm_timeout: int = 60

    # RAG
    rag_enabled: bool = False
    rag_vector_store: str = "chroma"  # chroma / milvus / pgvector
    rag_embedding_model: str = "bge-m3"

    # Redis（用于缓存 LLM 结果与会话上下文）
    redis_host: str = "127.0.0.1"
    redis_port: int = 6379
    redis_password: str = ""
    redis_db: int = 0

    # Java 网关回调（异步回写阅卷结果时使用）
    java_gateway_base_url: str = "http://ruoyi-exam-ai:9220"


settings = Settings()
