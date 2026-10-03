# AI 能力集成联调手册

> 面向要把 AI 跑起来的人。架构设计见 `AI_Agent架构设计与落地分析.md`，
> Agent 侧细节见 `ruoyi-agent/ruoyi-exam-agent/README.md`。

---

## 1. 全链路一览

```
浏览器
  │  /ai/**            /mark/task/{id}/ai
  ▼
网关 ruoyi-gateway (8080)
  │
  ├──────────────► ruoyi-exam-ai (9220)  ──HTTP──► ruoyi-exam-agent (9221, Python)
  │                     │                                │
  │                     │                                └── 模型供应商（DeepSeek / 通义 / ...）
  │                     │
  └──────────────► ruoyi-exam-mark (阅卷)
                        │  Dubbo: RemoteAiService
                        └────────► ruoyi-exam-ai ──HTTP──► agent
```

关键分工：

| 组件 | 职责 | 不做什么 |
| :--- | :--- | :--- |
| `ruoyi-exam-agent`（Python） | 提示词、Skill、模型主备切换、熔断、RAG | 不碰业务库 |
| `ruoyi-exam-ai`（Java 9220） | 业务语义 → Skill 调用、权限、日志 | 不存业务数据 |
| `ruoyi-exam-mark` | 阅卷流程、AI 建议分落库 | 不知道模型是谁 |
| 前端 | 抽屉交互、人工确认 | 不直接调 agent |

**AI 全程只出「建议」，写库动作全在业务侧。** 这是刻意的设计：模型判错不致命，模型擅自改分才是事故。

---

## 2. 启动顺序

```bash
# 1) 基础服务
#    Nacos 8848（必须）、MySQL 3306、Redis（可选，agent 可降级内存）

# 2) Python Agent
cd exam-back/ruoyi-agent/ruoyi-exam-agent
.venv/bin/python -m uvicorn app.main:app --host 0.0.0.0 --port 9221
# 验证：curl http://127.0.0.1:9221/health  → status UP，nacos.registered true

# 3) Java 服务（ruoyi-exam-ai 必须起，否则 mark 的 Dubbo 调用失败）
#    启动 ruoyi-exam-ai（9220）
# 验证：curl -H "Authorization: Bearer xxx" http://127.0.0.1:8080/ai/enabled
```

Agent 起不来，Java 侧**不会崩**：所有 AI 方法失败都返回 `success=false`，
前端 AI 按钮置灰，阅卷照常人工进行。

---

## 3. 网关路由（Nacos `ruoyi-gateway.yml`）

在 `spring.cloud.gateway.routes` 里加一条：

```yaml
- id: ruoyi-exam-ai
  uri: lb://ruoyi-exam-ai
  predicates:
    - Path=/ai/**
  filters:
    - StripPrefix=0        # AI 控制器写的是 @RequestMapping({"", "/ai"})，两种都兼容
    - CacheRequestFilter
    - ValidateCodeFilter
    - SaTokenFilter
```

> 如果你们的其它路由统一 `StripPrefix=1`，那这里也写 1，
> `AiController` 的 `{"", "/ai"}` 就是为这两种情况准备的。

---

## 4. 配置项

### 4.1 Java 侧（Nacos `ruoyi-exam-ai.yml`）

```yaml
exam:
  ai:
    enabled: true                        # 关掉后所有 AI 接口返回「不可用」
    url: http://127.0.0.1:9221           # 直连；置空则走 Nacos 服务发现
    service-name: ruoyi-exam-agent       # url 为空时生效
    api-prefix: /api/ai
    connect-timeout: 3000
    read-timeout: 60000
    mark-timeout: 45000
    batch-parallel: 4                    # 批量预评并发度
    retry-count: 1                       # 网络层重试；模型主备由 agent 自己做
    default-tenant: "000000"
```

### 4.2 阅卷侧（Nacos `ruoyi-exam-mark.yml`）

```yaml
exam:
  mark:
    ai:
      enabled: true    # 置 false 则退回 DefaultMarkAiServiceImpl（明确提示未接入）
```

> 两个实现**不能同时装配**（`MarkServiceImpl` 是单实例注入），
> 靠这个开关二选一，别手动去删 Bean。

### 4.3 Agent 侧（`.env`）

`AGENT_LLM_MOCK=true` 时不调真实模型、返回空结构，用于联调链路。
**生产必须置 false 并配 `AGENT_LLM_API_KEY`**，或往 `ry-cloud.ai_model_config` 插记录。

---

## 5. 权限

执行 `script/sql/update/update_exam_ai_menu.sql`（库：`ry-cloud`）：

| 权限标识 | 用途 |
| :--- | :--- |
| `exam:ai:list` | 模型清单、Skill 自测 |
| `exam:ai:edit` | 出题、质检、试卷审查、主观题评分 |

admin 超管默认全权限；其它角色在「角色管理」里勾选。
菜单 2130 是**隐藏目录**（没有独立页面），只用于承载权限。

---

## 6. 验证清单

```bash
# 1. agent 活着
curl http://127.0.0.1:9221/health

# 2. Nacos 能看到 agent
TOKEN=$(curl -s -X POST "http://127.0.0.1:8848/nacos/v1/auth/users/login" \
  --data-urlencode "username=nacos" --data-urlencode "password=nacos" \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['accessToken'])")
curl "http://127.0.0.1:8848/nacos/v1/ns/instance/list?serviceName=ruoyi-exam-agent&namespaceId=dev&accessToken=$TOKEN"

# 3. AI 可用（走网关，需要登录 token）
curl -H "Authorization: Bearer $LOGIN_TOKEN" http://127.0.0.1:8080/ai/enabled

# 4. 模型清单
curl -H "Authorization: Bearer $LOGIN_TOKEN" http://127.0.0.1:8080/ai/model/list

# 5. 端到端跑一个 Skill
curl -X POST -H "Authorization: Bearer $LOGIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"skillCode":"mark_score","input":{"stem":"简述TCP三次握手","full_score":10,"standard_answer":"SYN-SYN/ACK-ACK","answer_text":"客户端发SYN，服务端回SYN-ACK，客户端再发ACK"}}' \
  http://127.0.0.1:8080/ai/skill/run
```

---

## 7. 已接入的能力

| 能力 | 入口 | Skill | 是否写库 |
| :--- | :--- | :--- | :--- |
| 主观题 AI 预评 | 阅卷打分页「AI 预评」 | `mark_score` | 写建议分，最终分仍需教师确认 |
| AI 出题 | 题库页「AI 出题」 | `question_gen` | 教师勾选后保存 |
| 试题质检 | 出题时「自动质检」开关 | `question_audit` | 否 |
| 试卷审查 | 试卷页「AI 审查」 | `paper_review` | 否 |
| 错题归因 | 错题明细页「AI 诊断」 | `diagnosis` | 否 |
| 通用 Skill | `POST /ai/skill/run` | 任意 | 否 |

未接前端、接口已就绪：`/ai/mark/score`、`/ai/mark/batch`、`/ai/question/audit`。

---

## 8. 排障

| 现象 | 原因 | 处理 |
| :--- | :--- | :--- |
| 前端 AI 按钮一直是灰的 | `/ai/enabled` 返回 false | 查 agent 是否起、Java `exam.ai.url` 是否可达 |
| 阅卷点「AI 预评」报「服务不可用」 | agent 挂了，或 mark 连不上 ai 服务 | 看 `RemoteAiServiceImpl.enabled()`；确认 Nacos 上有 `ruoyi-exam-ai` |
| Nacos 看不到 agent | 见 README「为什么不用 nacos-sdk-python」 | 走 REST 注册；多网卡要配 `AGENT_NACOS_IP` |
| 出题返回空数组 | MOCK 模式，或模型返回的 JSON 解析失败 | 看 agent 日志；确认 `AGENT_LLM_MOCK=false` |
| 预评很慢 | 批量并发度太低 | 调 `exam.ai.batch-parallel`（agent 侧有信号量上限 16） |
| 分数明显不合理 | 模型没按要点给分 | 看 `ai_reason`；置信度 <0.6 前端会标「建议人工复核」 |
