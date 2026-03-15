"""共享的大模型工厂：OpenAI 兼容协议（SiliconFlow DeepSeek-V3）。"""
from langchain_openai import ChatOpenAI

from app.config import settings


def get_chat_model() -> ChatOpenAI:
    """构造 ChatOpenAI 实例（OpenAI 兼容协议）。"""
    return ChatOpenAI(
        api_key=settings.llm_api_key,
        base_url=settings.llm_base_url,
        model_name=settings.llm_model,
        temperature=settings.llm_temperature,
        max_tokens=settings.llm_max_tokens,
        request_timeout=settings.llm_timeout,
    )
