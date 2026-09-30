"""考试答疑 Agent：RAG 链，基于题库向量库检索，讲解错题背后知识点，不泄露原题答案。"""
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.runnables import RunnablePassthrough, RunnableLambda

from app.agents.llm import get_chat_model

SYSTEM_PROMPT = """你是考试答疑 Agent。基于检索到的教材/题库相关知识，帮助学生理解错题背后的知识点与解题思路。

重要约束：
1. 绝不直接给出原题的标准答案或选项字母；
2. 只做知识点讲解、思路引导和概念辨析；
3. 鼓励学生独立思考，可提供同类示例。

回答用中文，条理清晰。"""


def build_qa_chain(retriever):
    """构造 RAG 答疑链。

    :param retriever: 由知识库构建的向量检索器（FAISS）。
    """
    llm = get_chat_model()
    prompt = ChatPromptTemplate.from_messages([
        ("system", SYSTEM_PROMPT),
        ("human", "相关参考资料：\n{context}\n\n学生问题：{question}"),
    ])

    def format_docs(docs):
        return "\n\n".join(d.page_content for d in docs) if docs else "（未检索到相关资料）"

    chain = (
        {"context": retriever | RunnableLambda(format_docs),
         "question": RunnablePassthrough()}
        | prompt
        | llm
        | StrOutputParser()
    )
    return chain


def answer(qa_chain, message: str) -> str:
    """回答学生提问。"""
    return str(qa_chain.invoke(message))
