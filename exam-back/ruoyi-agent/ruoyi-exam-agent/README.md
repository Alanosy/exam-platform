# ruoyi-exam-agent

考试系统 AI 编排服务（Python + FastAPI）。端口 **9221**，注册到 Nacos 供 Java 网关调用。

详细的设计与落地分析见：`exam-back/docs/AI_Agent架构设计与落地分析.md`

---

## 1. 架构分层

```
L5 接入层   app/api/**         REST 接口，统一返回体 R<T>
L4 编排层   app/agent/**       Planner / Executor / Critic / Guardrail / Orchestrator
L3 能力层   app/skills/**      17 个 Skill（可插拔能力包）
            app/tools/**       15 个 Tool（回调 Java 业务能力）
            app/memory/**      短期/长期记忆（Redis，不可用时降级内存）
            app/rag/**         RAG 检索（关键词 BM25，无需向量库）
            app/prompts/**     17 条提示词 YAML，支持热加载
L2 网关层   app/gateway/**     主备切换 + 熔断 + 重试 + 审计（核心）
L1 协议层   app/llm/**         OpenAI 兼容协议客户端（httpx，不依赖 langchain）
```

**为什么不用 langchain**：只需要 `chat/completions` 一个能力，且要精确控制超时、
重试与主备链路；langchain 的版本组合极易打架，装 RAG 依赖还会拉进整个 torch。
现在全部依赖只有：fastapi / uvicorn / pydantic / httpx / nacos-sdk / pymysql / redis(可选)。

---

## 2. 启动

```bash
cd exam-back/ruoyi-agent/ruoyi-exam-agent

# 创建虚拟环境（conda base python 3.12）
/opt/anaconda3/bin/python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 启动（读取 .env）
python -m app.main
# 或
uvicorn app.main:app --host 0.0.0.0 --port 9221 --reload
```

启动后：
- 接口文档：http://127.0.0.1:9221/docs
- 健康检查：http://127.0.0.1:9221/health
- 自动注册到 Nacos（服务名 `ruoyi-exam-agent`）

### 没有真实 API Key 也能跑

`.env` 里 `AGENT_LLM_MOCK=true` 时，模型调用返回空结构，
可以验证「网关 → 解析 → 返回」全链路是否通畅。**仅用于联调，严禁生产使用。**

---

## 3. 测试

分三层，从快到慢：

```bash
cd exam-back/ruoyi-agent/ruoyi-exam-agent

# ① 单元测试（默认）：不连 MySQL / Nacos / Redis / Java 网关，也不需要真实 API Key
pip install -r requirements-dev.txt
pytest                      # 全量
pytest tests/test_gateway.py -v
pytest -k circuit -v

# ② 接口冒烟：服务真的跑起来之后打真实端口
python -m app.main                       # 或 uvicorn app.main:app --port 9221
python selftest.py                       # 结果写 selftest-report.txt
curl http://127.0.0.1:9221/health

# ③ 真实模型回归：配好 Key 后跑，会花钱
AGENT_LLM_MOCK=false AGENT_LLM_API_KEY=sk-xxx pytest -m integration
```

单测的环境由 `tests/conftest.py` 在 import app **之前**注入
（`AGENT_LLM_MOCK=true / AGENT_MODEL_SOURCE=mock / MYSQL=false / NACOS=false`），
因此**在没有 Key 的机器上也能全量回归**。

| 测试文件 | 守的是什么 |
|---|---|
| `tests/test_prompts.py` | 提示词加载、占位符渲染、JSON 花括号不被吃掉 |
| `tests/test_skills.py` | 17 个 Skill 注册完整、端到端能跑、批量失败隔离 |
| `tests/test_gateway.py` | 配置源降级、熔断状态机、全部失败必须抛错（不静默成功） |
| `tests/test_tools.py` | **护栏铁律**：高危工具未批准必须被拦 |
| `tests/test_api.py` | 统一返回体 R\<T\> 形状、Java 侧调用路径不变 |

---

## 4. 模型配置与主备切换

配置源按顺序尝试（`AGENT_MODEL_SOURCE=mysql,env`）：

1. **mysql**：只读 `ry-cloud.ai_model_config`
   ```sql
   SELECT ... FROM ai_model_config
   WHERE del_flag='0' AND status='0' AND tenant_id=?
   ORDER BY priority ASC, id ASC
   ```
   `priority` 最小的为主用，其余依次为备用。**表结构不用改**，`priority/weight/retry_count/timeout` 字段本来就有。
2. **env**：MySQL 读不到时降级。单模型用 `AGENT_LLM_*`，多模型用 `AGENT_LLM_MODELS`（JSON 数组）。

### 切换流程

```
取候选链（priority 升序）
  → 过滤熔断中的
  → 逐个尝试（单模型内重试 retry_count 次）
  → 全失败 → 抛 AiUnavailableError（**绝不静默降级为假成功**）
```

**必须配熔断**：不加熔断时，挂掉的模型会被每个请求试一遍，
200 人的批量阅卷会白等 200 次超时。状态机：

```
            连续失败 >= 3
HEALTHY ─────────────────► OPEN（冷却 60s）
   ▲                          │ 冷却结束
   └──── HALF_OPEN（放行 1 个探针）── 成功则恢复，失败回 OPEN
```

> ⚠️ `ai_model_config` 在 `ry-cloud`，本服务**只读不写**。
> 生产环境建议改为经 `RemoteModelConfigService`（Dubbo）获取，不要直连。

---

## 5. 接口清单

### 业务接口（Java 侧调用，路径与旧版保持一致）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/ai/question/generate` | AI 出题 |
| POST | `/api/ai/question/generate-audit` | 出题 + 质检（Reflection） |
| POST | `/api/ai/question/rewrite` | 改写扩量 |
| POST | `/api/ai/question/distractor` | 干扰项生成 |
| POST | `/api/ai/question/tag` | 知识点打标 |
| POST | `/api/ai/question/analysis` | 解析生成 |
| POST | `/api/ai/grade/auto` | **AI 批量预评（主观题）** |
| POST | `/api/ai/grade/single` | 单题预评 |
| POST | `/api/ai/recommend/wrong` | 个性化推题 |
| POST | `/api/ai/recommend/diagnose` | 错题归因 |
| GET | `/api/ai/recommend/profile/{user_id}` | 学情画像 |
| POST | `/api/ai/paper/analyze` | 试卷分析 |
| POST | `/api/ai/paper/difficulty` | 难度预估 |
| POST | `/api/ai/proctor/analyze` | 监考行为分析 |

### 通用能力接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/ai/skill/list` | 列出所有 Skill（含输入 schema） |
| POST | `/api/ai/skill/{code}/run` | **一个端点跑所有 Skill** |
| POST | `/api/ai/skill/{code}/batch` | 批量执行（单条失败不影响其它） |
| GET | `/api/ai/tool/list` | 工具清单 + 护栏策略 |
| POST | `/api/ai/tool/{code}/invoke` | 调用工具（回调 Java） |
| GET | `/api/ai/agent/plan/list` | 预置计划 |
| POST | `/api/ai/agent/plan/{task}/run` | 执行计划（Plan-and-Execute） |
| POST | `/api/ai/agent/orchestrate/vote` | Multi-Agent 投票（阅卷双评） |
| POST | `/api/ai/agent/orchestrate/pipeline` | 串行流水线 |
| POST | `/api/ai/agent/orchestrate/panel` | 并行专家组 |
| POST | `/api/ai/agent/reflect` | Reflection 自检 |
| POST | `/api/ai/agent/generate-and-audit` | 生成 + 质检组合拳 |

### 运维接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/health` | 健康检查（含模型/熔断/注册状态） |
| GET | `/api/ai/model/list` | 模型主备链（key 已脱敏） |
| GET | `/api/ai/model/health` | 熔断状态 + 调用统计 |
| POST | `/api/ai/model/reload` | 刷新配置缓存 |
| POST | `/api/ai/model/circuit/reset` | 手动复位熔断 |
| GET | `/api/ai/model/call-log` | AI 调用流水（含切换痕迹） |
| POST | `/api/ai/rag/ingest` | 灌入知识库 |
| POST | `/api/ai/rag/retrieve` | 检索 |

---

## 6. Skill 清单（17 个）

| code | 名称 | 域 |
|---|---|---|
| `question_gen` | 智能出题 | 命题 |
| `distractor_gen` | 干扰项生成 | 命题 |
| `question_rewrite` | 改写扩量 | 命题 |
| `knowledge_tag` | 知识点打标 | 命题 |
| `analysis_gen` | 解析生成 | 命题 |
| `question_audit` | **试题质检（Reflection）** | 命题 |
| `mark_score` | **主观题 AI 评分** | 阅卷 |
| `mark_reflect` | 评分自检 | 阅卷 |
| `code_judge` | 代码题判分 | 阅卷 |
| `scoring_calib` | 评分校准 | 阅卷 |
| `similarity_detect` | 雷同检测 | 阅卷 |
| `diagnosis` | 错题归因 | 学情 |
| `recommend` | 个性化推题 | 学情 |
| `paper_review` | 试卷审查 | 试卷 |
| `paper_difficulty` | 难度预估 | 试卷 |
| `proctor_analyze` | 监考分析 | 监考 |
| `report_write` | 报告撰写 | 运营 |

新增 Skill 两步：写 `prompts/<code>.yaml` → 在 `app/skills/registry.py` 加一行。

---

## 7. 八种 Agent 模式在本项目的落点

| 模式 | 落点 |
|---|---|
| Tool-Augmented | `app/tools/**`，13 个工具，全场景底座 |
| **Reflection** | `app/agent/critic.py` + `question_audit` / `mark_reflect`，命题与阅卷必配 |
| RAG | `app/rag/retriever.py`，一期关键词，向量库可选 |
| ReAct | `app/agent/executor.py`，每步结果回灌上下文 |
| Plan-and-Execute | `app/agent/planner.py`，5 套预置计划 |
| Multi-Agent | `app/agent/orchestrator.py`，pipeline / vote / panel |
| Memory-Augmented | `app/memory/store.py`，学情画像 + 评分偏好 |
| Autonomous Loop | **未开放**：碰分数/发布的动作一律 HITL |

**护栏铁律**：`submit_mark_score`、`publish_exam` 必须显式 `approved=true`；
`proctor_analyze` 的 `need_human` 恒为 true。

---

## 8. 与 Java 侧协作

```
[ruoyi-exam-mark] --|
[ruoyi-exam-question] --|--(Dubbo)--> [ruoyi-exam-ai:9220] --(REST)--> [ruoyi-exam-agent:9221]
[ruoyi-exam-paper]   --|                                                        |
                                                              LLM / RAG / Redis / MySQL(只读)
```

工具层路径约定 `/api/exam-tool/**`，由 `ruoyi-exam-ai` 实现后即可打通。
当前 Java 侧尚未实现这些端点，调用会返回提示信息（不是 500）。

---

## 9. 关键配置

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `AGENT_PORT` | 9221 | 端口 |
| `AGENT_NACOS_ENABLED` | true | 是否注册 Nacos |
| `AGENT_NACOS_USERNAME/PASSWORD` | nacos/nacos | 本机 Nacos 开了鉴权，必须填 |
| `AGENT_MODEL_SOURCE` | mysql,env | 配置源顺序 |
| `AGENT_MODEL_FUSE_THRESHOLD` | 3 | 连续失败几次熔断 |
| `AGENT_MODEL_FUSE_COOLDOWN` | 60 | 熔断冷却秒数 |
| `AGENT_MYSQL_*` | - | 只读 ai_model_config |
| `AGENT_LLM_MOCK` | false | 自测开关，严禁生产 |
| `AGENT_AGENT_REFLECTION_ENABLED` | true | 是否启用自检 |
| `AGENT_RAG_ENABLED` | true | RAG 开关 |

完整清单见 `.env.example`。

---

## 10. Docker

```bash
docker build -t ruoyi-exam-agent:1.0.0 .
docker run -d -p 9221:9221 --env-file .env ruoyi-exam-agent:1.0.0
```
