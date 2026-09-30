"""全局配置（Pydantic Settings，从环境变量 / .env 加载）。"""
import os

from dotenv import load_dotenv
from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict

load_dotenv()


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    # 服务
    server_host: str = "0.0.0.0"
    server_port: int = 8081

    # 数据库
    db_host: str = "127.0.0.1"
    db_port: int = 3306
    db_name: str = "db_exam"
    db_user: str = "root"
    db_password: str = "Aa112211"

    @property
    def db_url(self) -> str:
        return (
            f"mysql+pymysql://{self.db_user}:{self.db_password}"
            f"@{self.db_host}:{self.db_port}/{self.db_name}"
            "?charset=utf8mb4"
        )

    # JWT（与考试系统共享密钥，HS256）
    jwt_secret: str = ""

    # 大语言模型
    llm_api_key: str = ""
    llm_base_url: str = "https://api.siliconflow.cn/v1"
    llm_model: str = "deepseek-ai/DeepSeek-V3"
    llm_temperature: float = 0.6
    llm_max_tokens: int = 4096
    llm_timeout: int = 60

    # Embedding
    embedding_api_key: str = ""
    embedding_base_url: str = "https://api.siliconflow.cn/v1"
    embedding_model: str = "BAAI/bge-large-zh-v1.5"
    rag_max_results: int = 4
    rag_min_score: float = 0.55

    # LangSmith（通过环境变量自动启用）
    langsmith_enabled: bool = Field(
        default_factory=lambda: os.getenv("LANGCHAIN_TRACING_V2", "false").lower() == "true"
    )


settings = Settings()

# 同步写入 LangChain/LangSmith 期望的环境变量
if settings.langsmith_enabled:
    os.environ.setdefault("LANGCHAIN_TRACING_V2", "true")
    os.environ.setdefault("LANGCHAIN_API_KEY", os.getenv("LANGCHAIN_API_KEY", ""))
    os.environ.setdefault("LANGCHAIN_PROJECT", os.getenv("LANGCHAIN_PROJECT", "online-exam-agent"))
    os.environ.setdefault("LANGCHAIN_ENDPOINT", os.getenv("LANGCHAIN_ENDPOINT", "https://api.smith.langchain.com"))
