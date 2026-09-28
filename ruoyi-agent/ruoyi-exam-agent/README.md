# ruoyi-exam-agent

考试系统 AI 编排服务（Python + FastAPI + LangChain）。

## 定位

采用「AI 网关 + Agent 编排」架构：
- **Java 网关层** `ruoyi-exam-ai`（端口 9220）：对外门面，承接 Dubbo 调用，通过 REST 转发到本服务。
- **Python Agent 层** `ruoyi-exam-agent`（端口 9221，**不直接暴露网关**）：承载 LLM 编排、RAG 检索、Prompt 管理。

通信双轨：
- 同步低延迟场景（出题、推荐、试卷分析）走 HTTP REST。
- 异步批量场景（批量阅卷、训练样本生成）走 RocketMQ（Java 网关消费后回调本服务）。

## 能力

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| AI 出题 | POST | `/api/ai/question/generate` | 按知识点/难度/题型生成题目，可选 RAG |
| AI 阅卷 | POST | `/api/ai/grade/auto` | 主观题语义评分，返回评分点 + 置信度 |
| AI 推荐 | POST | `/api/ai/recommend/wrong` | 错题本驱动的个性化推题 |
| 试卷分析 | POST | `/api/ai/paper/analyze` | 难度分布、知识点覆盖、改进建议 |
| 健康检查 | GET  | `/health` | 用于 Nacos / 网关探活 |

## 启动

```bash
# 1. 安装依赖（建议 Python 3.11+）
python -m venv .venv
.venv\Scripts\activate          # Windows
# source .venv/bin/activate     # Linux/Mac
pip install -r requirements.txt

# 2. 配置环境变量（可写入 .env）
set AGENT_LLM_API_KEY=sk-xxx
set AGENT_LLM_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
set AGENT_LLM_MODEL=qwen-plus
set AGENT_NACOS_SERVER=127.0.0.1:8848
set AGENT_NACOS_NAMESPACE=dev

# 3. 启动
uvicorn app.main:app --host 0.0.0.0 --port 9221 --reload
```

## 配置项

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `AGENT_HOST` | 0.0.0.0 | 监听地址 |
| `AGENT_PORT` | 9221 | 监听端口 |
| `AGENT_NACOS_ENABLED` | true | 是否注册到 Nacos |
| `AGENT_NACOS_SERVER` | 127.0.0.1:8848 | Nacos 地址 |
| `AGENT_NACOS_NAMESPACE` | dev | Nacos 命名空间 |
| `AGENT_LLM_PROVIDER` | openai | LLM 提供方 |
| `AGENT_LLM_BASE_URL` | https://dashscope.aliyuncs.com/compatible-mode/v1 | LLM API 地址 |
| `AGENT_LLM_API_KEY` | (必填) | LLM API Key |
| `AGENT_LLM_MODEL` | qwen-plus | 模型名 |
| `AGENT_LLM_TEMPERATURE` | 0.2 | 采样温度 |
| `AGENT_RAG_ENABLED` | false | 是否启用 RAG |
| `AGENT_RAG_VECTOR_STORE` | chroma | 向量库类型 |
| `AGENT_REDIS_HOST` | 127.0.0.1 | Redis 地址 |

## Docker

```bash
docker build -t ruoyi-exam-agent:1.0.0 .
docker run -d -p 9221:9221 --env-file .env ruoyi-exam-agent:1.0.0
```

## LLM 切换

通过环境变量即可切换后端，无需改代码：

- 通义千问：`AGENT_LLM_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1` + `AGENT_LLM_MODEL=qwen-plus`
- DeepSeek：`AGENT_LLM_BASE_URL=https://api.deepseek.com/v1` + `AGENT_LLM_MODEL=deepseek-chat`
- OpenAI：`AGENT_LLM_BASE_URL=https://api.openai.com/v1` + `AGENT_LLM_MODEL=gpt-4o-mini`
- 自托管 vLLM：`AGENT_LLM_BASE_URL=http://vllm:8000/v1` + `AGENT_LLM_MODEL=Qwen2.5-14B-Instruct`

## 与 Java 网关协作

```
[ruoyi-exam-question] --|
[ruoyi-exam-paper]   --|--(Dubbo)--> [ruoyi-exam-ai:9220] --(REST)--> [ruoyi-exam-agent:9221]
[ruoyi-exam-answer]  --|                                                       |
[ruoyi-exam-mark]    --|                                                       |
                                                            LLM / RAG / Redis
```

Java 侧调用方通过 `@DubboReference` 调 `RemoteExamAiService`，
内部由 `RestTemplate` / `WebClient` 走服务发现（`lb://ruoyi-exam-agent`）打到本服务。
