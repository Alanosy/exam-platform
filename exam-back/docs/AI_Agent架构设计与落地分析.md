# exam-platform AI / Agent 架构设计与落地分析

> 版本：v1.0 ｜ 日期：2026-10-02 ｜ 视角：架构设计 + FDE（Forward Deployed Engineer）落地视角
>
> 本文回答六个问题：
> 1. 系统里哪些地方可以加 AI？
> 2. Agent 整体架构怎么设计？
> 3. ReAct / Plan-and-Execute / Multi-Agent / Reflection / Tool-Augmented / Memory-Augmented / RAG / Autonomous Loop 各自适合什么、哪些要混合？
> 4. 哪些能力值得沉淀成 Skill？
> 5. 多模型主备切换（`ai_model_config`）怎么落地？
> 6. 从 FDE 视角，先做什么、怎么交付、有什么坑？

---

## 一、现状盘点：手里已经有什么牌

### 1.1 已建成的地基（这些都是白捡的）

| 资产 | 位置 | 状态 | 对 AI 的意义 |
| :--- | :--- | :--- | :--- |
| `ai_model_config` 表 | `ry-cloud` | ✅ 已有，含 `priority/weight/retry_count/timeout` | 模型网关的配置源，**已预留主备与重试字段** |
| 模型配置管理页面 | `exam-front/src/views/system/modelConfig` | ✅ 已有 | 运维入口不用再做 |
| `model_type` / `model_name` 字典 | `ry-cloud.sys_dict_*` | ✅ 已有 | 前端下拉框已就绪 |
| `ruoyi-api-exam-ai` | `exam-back/ruoyi-api/` | ⚠️ **空模块** | Dubbo 契约层已占好位 |
| `ruoyi-exam-ai`（921?） | `exam-back/ruoyi-modules/` | ⚠️ **只有启动类** | 实现层待填 |
| `exam_mark_item.ai_score / ai_reason / ai_status / ai_model / ai_time` | `ry-exam` | ✅ **字段已预留** | **AI 阅卷的数据模型早就设计好了** |
| `MarkAiService` + `DefaultMarkAiServiceImpl` | `ruoyi-exam-mark` | ✅ 抽象 + 桩实现 | **教科书级的接入点，换实现即可上线** |
| `MarkAiBo` / `MarkAiResult` | `ruoyi-exam-mark` | ✅ 契约完整 | 入参出参不用重新设计 |
| `exam_proctor_session` / `_event` / `_snapshot` | `ry-exam` | ✅ 含 `risk_score`、`switch_count`、事件明细 | 作弊行为序列 → AI 判定的天然语料 |
| `wrong_question` / `wrong_review_record` | `ry-exam` | ✅ 含 `wrong_count/master_status` | 学情记忆的现成载体 |
| 11 个 Dubbo `Remote*Service` | `ruoyi-api-exam-*` | ✅ 可用 | **Agent 工具层不用从零写** |
| `paper.random_rule` / `question_shuffle` / `option_shuffle` | `ry-exam.paper` | ✅ 已有 | 组卷策略可参数化 |

**结论：这个项目的 AI 化不是从零开始，而是「桩都打好了，只差填实现」。** 尤其是 AI 阅卷，`MarkAiService` 的接口注释直接写了「接 ruoyi-exam-ai 时只需要换一个实现类，上层打分逻辑不动」——这是明确的起跑线。

### 1.2 当前缺口（必须补的）

| 缺口 | 影响 | 建议 |
| :--- | :--- | :--- |
| **`question` 表无知识点/标签字段** | 无法做知识点覆盖分析、精准组卷、学情归因 | 新增 `knowledge_points`(JSON) / `tags` / `source` / `ai_generated` / `quality_score` / `review_status` |
| 无向量库 | RAG 无从落地 | 一期用 MySQL 全文 + 关键词兜底，二期上 pgvector / Milvus（**不要一上来就上向量库**） |
| 无 AI 调用日志表 | 无法核算成本、无法排查、无法审计 | 新建 `ai_call_log` |
| 无 Prompt 管理 | Prompt 散在代码里，改一次发一次版 | 新建 `ai_prompt_template`（**租户级可覆写**） |
| 无 Agent 任务表 | 长任务无法追踪、断点续跑 | 新建 `ai_agent_task` / `ai_agent_step` |
| `model_type` 字典只有 DeepSeek | 单一供应商风险 | 扩 OpenAI 兼容协议、通义、豆包、本地 vLLM/ollama |

### 1.3 题型矩阵（决定 AI 能干多少活）

```
SINGLE        单选      ┐
MULTIPLE      多选      ├─ 客观题：规则判分，AI 只服务「生成/质检」
JUDGE         判断      │
BLANK         填空      ┘（填空有歧义，AI 可做模糊匹配）
MATCH         匹配      ┐
SHORT_ANSWER  简答      │
ESSAY         论述      ├─ 主观题：★ AI 阅卷主战场
CODE          代码编程  │  （CODE 可结合代码执行器做半自动判分）
UPLOAD_FILE   文件上传  ┘  （需 OCR/多模态解析，二期）
```

**9 种题型，其中 4 类主观题是 AI 阅卷的直接受益面。**

---

## 二、AI 加持点全景图

按「价值 × 可行性」排序，★ 越多越优先。

### 2.1 阅卷域（价值最高，地基最齐）

| # | 场景 | 说明 | 优先级 |
| :-- | :--- | :--- | :---: |
| 2.1.1 | **AI 主观题预评** | 给建议分 + 评分理由，老师确认或微调。`ai_score/ai_reason` 直接落库 | ★★★★★ |
| 2.1.2 | **评分一致性校准** | 同一题多人阅卷时，AI 标出"离群评分"（某老师明显偏松/偏紧），触发复核 | ★★★★ |
| 2.1.3 | **批量预阅 + 抽样复核** | AI 全量预评，老师只复核高分区间 / 与 AI 分歧大的 | ★★★★ |
| 2.1.4 | **代码题辅助判分** | 结合代码执行器跑测试用例 + AI 评代码质量/规范 | ★★★ |
| 2.1.5 | **作答雷同检测** | 主观题答案相似度聚类，辅助判定抄袭 | ★★★ |
| 2.1.6 | **异常作答识别** | 空白、乱码、答非所问、复制题干 → 快速标记零分 | ★★★★ |

### 2.2 命题域（痛点最强，老师最想要）

| # | 场景 | 说明 | 优先级 |
| :-- | :--- | :--- | :---: |
| 2.2.1 | **从资料生成试题** | 上传课件/教材/制度文档 → RAG 抽取 → 生成单选/多选/判断 | ★★★★★ |
| 2.2.2 | **试题改写与扩量** | 已有 20 题 → 变体扩到 100 题（换场景、换数值、换表述） | ★★★★ |
| 2.2.3 | **干扰项生成** | 只给了正确答案，AI 生成合理干扰项（**干扰项质量决定选择题质量**） | ★★★★ |
| 2.2.4 | **试题质检** | 查答案唯一性、题干歧义、选项长度暗示、知识点与难度是否匹配 | ★★★★★ |
| 2.2.5 | **知识点/难度标注** | 批量给存量题打标（补 1.2 的字段缺口） | ★★★★ |
| 2.2.6 | **解析生成** | 已有题干和答案，批量补 `analysis` | ★★★★ |

### 2.3 组卷域

| # | 场景 | 优先级 |
| :-- | :--- | :---: |
| 2.3.1 | **按大纲智能组卷**：给"知识点分布 + 难度分布 + 总分 + 时长"自动选题 | ★★★★ |
| 2.3.2 | **组卷后校验**：覆盖度、重复度、难度曲线、预计用时 | ★★★★ |
| 2.3.3 | **平行卷生成**：A/B 卷等价性校验（防作弊场景刚需） | ★★★★ |
| 2.3.4 | **试卷难度预估**：不考就知道这场考试大概什么分 | ★★★ |

### 2.4 学情域（考生侧，感知最强）

| # | 场景 | 优先级 |
| :-- | :--- | :---: |
| 2.4.1 | **个人学情诊断**：基于 `wrong_question` + 历次成绩 → 薄弱知识点画像 | ★★★★★ |
| 2.4.2 | **错题归因**：不只是"选错了"，而是"概念不清 / 计算失误 / 审题偏差" | ★★★★ |
| 2.4.3 | **个性化推题**：按掌握度（SM-2 类遗忘曲线）推复习题 | ★★★★ |
| 2.4.4 | **备考助手**：考生问答，但不能直接给答案（**需约束为"引导式答疑"**） | ★★★ |

### 2.5 监考域

| # | 场景 | 优先级 |
| :-- | :--- | :---: |
| 2.5.1 | **作弊行为序列分析**：`exam_proctor_event` 时序 → 判定"切屏是查资料还是误触" | ★★★★ |
| 2.5.2 | **风险分动态校准**：现有 `risk_score` 是规则算的，AI 可结合上下文降噪 | ★★★ |
| 2.5.3 | **监考报告自动生成**：一场考试结束，AI 汇总异常考生清单 + 证据链 | ★★★★ |

### 2.6 运营/管理域

| # | 场景 | 优先级 |
| :-- | :--- | :---: |
| 2.6.1 | **考试数据分析助手**：自然语言问"上个月哪场考试通过率最低"→ 查库 → 出结论 | ★★★★ |
| 2.6.2 | **考务 Copilot**：一句话办完"创建一场安全合规考试，100 人，下周三" | ★★★ |
| 2.6.3 | **制度问答机器人**：考试规则、请假、补考政策的 RAG 问答 | ★★★ |
| 2.6.4 | **证书文案生成** | ★★ |

---

## 三、Agent 总体架构设计

### 3.1 分层架构

```
┌──────────────────────────────────────────────────────────────┐
│  L5 接入层  管理后台 Vue / 考生端 Vue / OpenAPI / Webhook      │
│             AI 面板 · 审阅确认台 · 对话窗 · 任务进度            │
└──────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────┐
│  L4 Agent 编排层  ruoyi-exam-ai                                │
│   ├─ AgentRegistry（场景 → Agent 映射）                        │
│   ├─ Planner（Plan-and-Execute 的规划器）                      │
│   ├─ Executor（ReAct 循环：Thought → Action → Observation）    │
│   ├─ Critic（Reflection 的批评者）                             │
│   ├─ Orchestrator（Multi-Agent 调度：串行/并行/辩论/投票）      │
│   └─ Guardrail（步数上限 / 超时 / 敏感动作拦截 / 人工审批闸门）  │
└──────────────────────────────────────────────────────────────┘
        ↓                ↓                 ↓              ↓
┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ L3 Skill 层  │ │ L3 工具层     │ │ L3 记忆层     │ │ L3 知识层     │
│ 可插拔能力包 │ │ Dubbo 适配器  │ │ 短期/长期/语义 │ │ RAG 检索      │
│ 出题/质检/   │ │ Remote*Service│ │ 租户画像/偏好  │ │ 资料/题库/    │
│ 评分/归因…   │ │ → Agent Tool  │ │ Redis+MySQL   │ │ 历史卷        │
└──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────┐
│  L2 模型网关层  AiModelRouter                                  │
│   配置加载(ai_model_config) → 主备排序 → 调用 → 失败切换        │
│   → 熔断降级 → 重试 → 流式/非流式 → Token 计量 → 审计日志        │
└──────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────┐
│  L1 模型供应商  DeepSeek / OpenAI 兼容 / 通义 / 豆包 / 本地 ollama│
└──────────────────────────────────────────────────────────────┘
```

### 3.2 为什么这么分层

- **L1/L2 必须独立**：供应商会换、价格会变、会挂。业务代码永远不直接碰 `api_key`。
- **L3 工具层是复用现成的**：11 个 `Remote*Service` 直接包成 Tool，不要重写业务逻辑。这是本项目最大的成本优势。
- **L4 是唯一"新东西"**：编排层从零写，但要做得薄——Spring AI / LangChain4j 能覆盖的部分不要自研，只自研"考试领域特有"的编排（评分仲裁、出题质检流水线）。
- **Guardrail 是一等公民，不是补丁**：教育评分是高风险决策，Agent 必须内建"自动动作白名单"。

### 3.3 模块落地位置

```
ruoyi-api/ruoyi-api-exam-ai/           ← 已存在，填 Dubbo 接口
    RemoteAiService.java               通用 AI 能力（chat / 结构化输出）
    RemoteAiMarkService.java           阅卷专用（被 ruoyi-exam-mark 调用）
    RemoteAiQuestionService.java       命题专用
    domain/RemoteAi*{Bo,Vo}.java

ruoyi-modules/ruoyi-exam-ai/           ← 已存在，填实现
    gateway/     AiModelRegistry / AiModelRouter / AiClient / 熔断
    skill/       QuestionGenSkill / QuestionAuditSkill / MarkScoreSkill ...
    tool/        QuestionTool / PaperTool / MarkTool / StatTool ...
    agent/       Planner / Executor / Critic / Orchestrator
    memory/      ShortTermMemory / LongTermMemory / TenantProfile
    rag/         Ingest / Retriever / Reranker
    domain/      ai_call_log / ai_prompt_template / ai_agent_task 实体
```

> ⚠️ **跨库约束**：`ai_model_config` 在 `ry-cloud`（system 库），`ruoyi-exam-ai` 不能直连。
> 必须在 `ruoyi-api-system` 新增 `RemoteModelConfigService`，通过 Dubbo 取配置；
> `api_key` 在 system 侧做脱敏返回或在 ai 侧用租户密钥信封解密，**不要明文在全网传输**。

---

## 四、八种 Agent 架构模式：适配分析

> 判断标准：任务**不确定性**高低 × **错误代价**高低 × **步骤**多少。

### 4.1 逐个体检

#### ① Tool-Augmented（工具增强）—— 全场景底座，必选

| 项 | 结论 |
| :--- | :--- |
| 适配度 | **★★★★★ 所有场景的必要条件** |
| 适合 | 一切。没有工具调用的 LLM 在这个系统里几乎是废物——它不知道题库里有什么题目 |
| 不适合 | 纯文本润色类（如证书文案生成）可以不挂工具 |

**本项目的工具清单（直接由 Remote*Service 包装）：**

```
QuestionTool    searchQuestions / getQuestion / createQuestion / updateQuestion
                / checkDuplicate / batchTag
PaperTool       createPaper / addQuestions / calcTotalScore / validatePaper
ExamTool        createExam / publishExam / listExams / updateExamConfig
MarkTool        listPendingItems / getItemDetail / submitScore / listMarkedItems
StatTool        examOverview / questionStats / studentStats / scoreDistribution
PracticeTool    getWrongQuestions / getMasteryStatus
ProctorTool     getSessionEvents / getRiskScore / listRiskySessions
CertTool        issueCert / queryCert
```

**关键约束**：写操作类工具（`publishExam` / `submitScore`）必须标记 `riskLevel=WRITE`，
由 Guardrail 决定是否放行或转人工审批。

---

#### ② RAG（检索增强生成）

| 项 | 结论 |
| :--- | :--- |
| 适配度 | ★★★★★ 命题域 / ★★★★ 答疑域 / ★★★ 阅卷域 |
| **最适合** | ① 从课件教材文档出题；② 考试制度/规则问答；③ 组卷时检索历史优质题；④ 主观题评分时检索"该题历史高分答卷"作为评分锚点 |
| 不适合 | 客观题判分（规则即可）；纯数值统计 |

**落地要点**：
- 一期**不要上向量库**。题库是结构化的，用 `bank_id + question_type + difficulty + 关键词` 检索就够，成本为零。
- 文档类 RAG（课件→出题）才需要向量化，且只在有真实需求时引入（推荐 MySQL 8 全文 → 二期 pgvector）。
- **评分锚点 RAG 是被低估的一招**：给 AI 阅卷时附上 3 份历史满分答卷 + 3 份零分答卷，评分一致性会显著提升。

---

#### ③ ReAct（推理-行动交替）

| 项 | 结论 |
| :--- | :--- |
| 适配度 | ★★★★ |
| **最适合** | 中等不确定、需要"边查边想"的任务：<br>• **智能组卷**：查题 → 发现某知识点题量不足 → 决定放宽难度或生成新题 → 再校验总分<br>• **试题查重**：检索相似题 → 比对 → 判定是否重复 → 给出合并建议<br>• **学情诊断**：查成绩 → 查错题 → 定位薄弱点 → 查该知识点题目 → 出建议 |
| 不适合 | 步骤固定不变的（用 Plan-and-Execute 更省 token）；单步就能完成的 |

**注意**：ReAct 的 token 消耗随步数线性增长，组卷场景要设 `maxSteps=12` 硬上限。

---

#### ④ Plan-and-Execute（先规划后执行）

| 项 | 结论 |
| :--- | :--- |
| 适配度 | ★★★★★ 长流程 / ★★ 短任务 |
| **最适合** | 步骤多但路径可预判的"批处理"型任务：<br>• **一场考试的完整筹备**：定大纲 → 生成题 → 组卷 → 配防作弊 → 校验 → 发布<br>• **批量阅卷任务编排**：拆 200 份答卷 → 并行预评 → 汇总分歧 → 出复核清单<br>• **学情报告生成**：取数 → 分项统计 → 归因 → 成文 → 配图 |
| 不适合 | 需要根据中间结果大幅改道的任务（纯 Plan 会僵化） |

**本项目推荐的做法**：`Plan → 逐步 Execute → 每步结束 Re-Plan`。
即 **Plan-and-Execute 与 ReAct 混合**——先把大流程定下来省 token，但允许执行中动态重规划，
避免"计划赶不上变化"时整个任务崩掉。

---

#### ⑤ Multi-Agent（多智能体协作）

| 项 | 结论 |
| :--- | :--- |
| 适配度 | ★★★★★ 质量敏感场景 |
| **最适合** | 角色天然分工的场景（本项目有 3 个绝佳场景）：<br><br>**A. 命题流水线**<br>　出题 Agent → 审题 Agent（挑错）→ 难度校准 Agent → 终审（人）<br><br>**B. 阅卷仲裁**<br>　初评 Agent A（严格）→ 初评 Agent B（宽松）→ 分歧 > 阈值时<br>　仲裁 Agent 复核 → 仍分歧 → 转人工<br><br>**C. 试卷审查团**<br>　知识点覆盖审查员 + 难度分布审查员 + 歧义/答案唯一性审查员<br>　并行跑，汇总出审查报告 |
| 不适合 | 简单单步任务（杀鸡用牛刀，成本翻倍） |
| 拓扑选择 | 串行流水线（命题）／并行投票（阅卷）／辩论式（质量争议） |

**成本警告**：Multi-Agent 的 token 消耗是单 Agent 的 2-5 倍。
**只在"错误代价 > AI 成本"时用**——题目出错会毁掉一场考试，值得；证书文案不值得。

---

#### ⑥ Reflection（自我反思 / 批评者循环）

| 项 | 结论 |
| :--- | :--- |
| 适配度 | **★★★★★ 本项目性价比最高的模式** |
| **最适合** | 输出正确性要求高的地方：<br>• **AI 出题后自校验**：答案是否唯一正确？干扰项是否真的有干扰性？题干有无歧义？<br>• **AI 阅卷二次校验**：我的给分和参考答案要点匹配吗？是不是被答案长度忽悠了？<br>• **生成的解析是否与题干一致** |
| 不适合 | 主观创意类、无客观对错标准的 |

**为什么性价比最高**：Reflection 只需额外 1 次调用，就能拦掉大部分低级错误，
而 Multi-Agent 需要 2-5 次。**在命题和阅卷这两个"错不起"的域，Reflection 是标配。**

**考试域专用 Critic 清单**（可直接写成 Prompt）：
```
✓ 选择题：是否存在第二个正确答案？
✓ 选择题：正确答案是否在长度/表述上有明显暗示？
✓ 填空题：是否存在多个可接受的答案写法？
✓ 代码题：题目是否给出了充分的输入输出约束？
✓ 通用：题干是否有歧义表述（"可能""一般""通常"）？
✓ 通用：难度标注与实际认知负荷是否匹配？
✓ 评分：给分理由是否引用了参考答案要点，而非泛泛而谈？
✓ 评分：是否因答案冗长而给了虚高分？
```

---

#### ⑦ Memory-Augmented（记忆增强）

| 项 | 结论 |
| :--- | :--- |
| 适配度 | ★★★★★ 学情域 / ★★★★ 阅卷域 / ★★★ 命题域 |
| **最适合** | 需要跨会话积累的个性化场景：<br>• **考生学情记忆**（长期薄弱知识点、答题速度、易错题型）→ `wrong_question` 已是现成载体<br>• **老师阅卷风格记忆**（某老师历来偏松 0.5 分，AI 预评时自动对齐）<br>• **租户出题风格记忆**（某企业偏好场景化实操题而非概念题）<br>• **短期记忆**：一次组卷会话内的上下文（已选题目、剩余分值） |

**三层记忆设计**：
```
L1 短期记忆  Redis，TTL 30min    会话内上下文（组卷中间态、对话历史）
L2 长期记忆  MySQL              结构化画像（学情、评分偏好、出题风格）
L3 语义记忆  向量库（二期）       历史相似任务、历史优质题、历史高分答卷
```

**隐私红线**：考生个人学情属于个人信息，长期记忆必须
① 租户隔离 ② 可导出可删除 ③ 不做跨租户迁移。

---

#### ⑧ Autonomous Loop（自主循环）

| 项 | 结论 |
| :--- | :--- |
| 适配度 | ★★★ 后台任务 / **★ 前台决策禁用** |
| **最适合** | 低风险、可回滚、无人值守的后台巡检：<br>• 题库质量巡检（定时扫描：无解析、无答案、被标记错误的题）<br>• 阅卷任务进度自动推进与超时提醒<br>• 考试结束后自动触发批改 → 出报告 → 推通知 |
| **绝对不适合** | ❌ 自动发布考试 ❌ 自动定最终分 ❌ 自动判定作弊 ❌ 自动给学生发成绩 |
| 铁律 | **凡是会影响考生命运的动作，一律不放进 Autonomous Loop，必须 HITL（人在回路）** |

**安全设计**：给每个自主任务配 `blast radius`（影响半径）：
- `read-only` → 可自主
- `draft-only`（只写草稿态） → 可自主
- `affects-score` / `affects-publish` → **必须人工审批**

---

### 4.2 混合使用矩阵（重点）

单打独斗的模式在本项目基本不够用。以下是推荐的组合配方：

| 场景 | 主模式 | 辅模式 | 组合理由 | HITL |
| :--- | :--- | :--- | :--- | :---: |
| **AI 主观题阅卷** | Tool-Augmented | **Reflection** + Multi-Agent(双评) + RAG(评分锚点) | 工具取卷；AI 自校验给分理由；双评分歧时仲裁；历史答卷做锚点 | ✅ 必须 |
| **智能出题** | RAG | **Reflection** + Multi-Agent(审题) | 资料检索出题；自检答案唯一性；审题 Agent 挑错 | ✅ 必须 |
| **试题质检（存量治理）** | **Reflection** | Autonomous Loop + Tool | 定时扫描；逐题自检；只产出报告不自动改 | ✅ 建议 |
| **智能组卷** | Plan-and-Execute | **ReAct**(重规划) + Tool | 先规划大纲；题量不足时边查边调 | ⚠️ 发布前确认 |
| **平行卷 / A-B 卷** | Multi-Agent | RAG + Reflection | 检索等价题；交叉校验难度与覆盖度 | ⚠️ 发布前确认 |
| **学情诊断与推题** | Memory-Augmented | ReAct + RAG + Tool | 读长期画像；边查边归因；检索适配题目 | ❌ 可自主 |
| **考试数据分析助手** | ReAct | Tool + RAG(SQL 示例库) | 边查库边推理；自然语言出结论 | ❌ 只读可自主 |
| **考务 Copilot** | Plan-and-Execute | Tool + Multi-Agent | 一句话拆解成多步考务动作 | ✅ 写操作确认 |
| **监考报告生成** | Tool-Augmented | RAG + Reflection | 取事件序列；对照规则；自检证据链完整性 | ✅ 判定作弊必须人工 |
| **制度问答机器人** | RAG | Memory(对话) + Reflection | 检索制度文档；记住上下文；自检引用是否准确 | ❌ 可自主 |
| **题库日常巡检** | Autonomous Loop | Reflection + Tool | 无人值守；只出清单不改数据 | ❌ 可自主 |

**混合的三条原则**：
1. **Tool-Augmented 是地基**，任何场景都要挂，不单独算一个"模式"。
2. **Reflection 是质量保险**，凡是输出会被人当真（题目、分数）的，都加一层。
3. **越接近"影响考生结果"，越要往 HITL 收**，而不是往自主放。

---

## 五、Skill 化清单

> Skill = 输入输出清晰、可独立测试、可复用、可单独计费/计量的能力单元。
> 判断标准：**换个场景还能用吗？能单独写单测吗？**

### 5.1 强烈建议做 Skill 的（P0）

| Skill | 输入 | 输出 | 复用场景 |
| :--- | :--- | :--- | :--- |
| **QuestionGenSkill** | 资料片段 / 知识点 / 题型 / 难度 / 数量 | 结构化试题 JSON | 出题、扩量、平行卷 |
| **DistractorGenSkill** | 题干 + 正确答案 | 3-4 个干扰项 | 单选、多选生成 |
| **QuestionAuditSkill** | 单道题 | {问题列表, 严重度, 修改建议} | 出题后质检、存量题治理 |
| **QuestionRewriteSkill** | 原题 + 改写策略（换场景/换数值/反向） | 变体题 | 扩量、防泄露 |
| **MarkScoreSkill** | 题干+参考答案+评分要点+考生作答+满分 | {分数, 理由, 要点命中情况} | 简答/论述/填空 |
| **CodeJudgeSkill** | 代码 + 语言 + 测试用例 + 评分维度 | {通过率, 质量分, 评语} | 代码题 |
| **ScoringCalibSkill** | 该题所有历史人工分 + AI 分 | {偏差系数, 校准建议} | 阅卷一致性 |
| **KnowledgeTagSkill** | 题干 + 答案 | {知识点[], 难度, 认知层次} | 存量题打标（补字段缺口） |
| **AnalysisGenSkill** | 题干 + 答案 | 解析文本 | 批量补 `analysis` |

### 5.2 建议做 Skill 的（P1）

| Skill | 说明 |
| :--- | :--- |
| **DiagnosisSkill** | 错题 → 归因（概念不清/计算失误/审题偏差/时间不足） |
| **RecommendSkill** | 学情画像 → 推题序列（结合遗忘曲线） |
| **PaperReviewSkill** | 试卷 → 覆盖度/难度曲线/重复度/预计用时报告 |
| **PaperDifficultyPredictSkill** | 试卷 → 预估平均分与通过率 |
| **ProctorAnalyzeSkill** | 事件时序 → {可疑度, 证据链, 建议处置} |
| **ReportWriteSkill** | 统计数据 → 自然语言分析报告 |
| **SimilarityDetectSkill** | 多份主观作答 → 雷同聚类 |
| **AnswerQualityFilterSkill** | 作答 → 是否空白/乱码/复制题干 |

### 5.3 不建议做 Skill 的

- **客观题判分**：规则实现，不要 LLM，慢且没必要。
- **考试发布/成绩写回**：这是业务流程不是 Skill，放在 Agent 的 Write Tool 里加审批。
- **证书渲染**：模板引擎的事。

### 5.4 Skill 的工程约定（建议）

```java
public interface AiSkill<IN extends SkillInput, OUT extends SkillOutput> {
    String code();                    // 唯一编码，对应 ai_prompt_template.skill_code
    String version();                 // Prompt 版本，用于灰度与回滚
    int maxInputTokens();             // 超长输入的分批策略依据
    OUT execute(IN input, AiContext ctx);   // ctx 携带 tenantId / userId / traceId
    default boolean requireHumanReview() { return false; }
}
```

每个 Skill 必须：
① 有独立的单元测试（固定 case + 边界 case）
② 有 Golden Set（人工标注的标准输入输出集），改 Prompt 后跑回归
③ 记录 `ai_call_log`（skill_code / model / tokens / latency / success）

---

## 六、多模型主备切换设计（`ai_model_config`）

### 6.1 需求分析

> 原始需求：**配置多个，只用启用的；启用多个时先用一个，报错了切换到备用。**

这是经典的 **Active-Standby Failover**，不是负载均衡。
表里已有 `priority`（默认 10）、`weight`（默认 100）、`retry_count`（默认 1）、`timeout`（默认 60000ms）——**字段全够了，不用改表**。

### 6.2 选型规则

```sql
SELECT * FROM ai_model_config
WHERE tenant_id = ?            -- 租户隔离
  AND del_flag = '0'
  AND status   = '0'           -- 0 = 启用
ORDER BY priority ASC, id ASC  -- priority 小者优先 → 主用；其余为备用链
```

> `weight` 字段**本期不参与决策**，保留给未来可能的加权轮询/灰度分流。

### 6.3 状态机（避免每次失败都全量切换）

```
                 连续失败 >= retry_count
   HEALTHY ─────────────────────────────► DEGRADED（切换备用，仍定期探活）
      ▲                                        │
      │        探活成功                         │ 连续失败 >= 阈值(如 3)
      └────────────────────────────────────────┤
                                               ▼
                                          FUSE_OPEN（熔断，冷却 60s）
                                               │ 冷却结束
                                               ▼
                                          HALF_OPEN（放行 1 个探针请求）
                                            │            │
                                      成功 ─┘            └─ 失败 → 回 FUSE_OPEN
```

**为什么需要熔断**：没有熔断时，一个挂掉的模型会被每个请求都试一遍，
一场 200 人考试的批量阅卷会白白浪费 200 次超时等待。

### 6.4 调用流程

```
AiModelRouter.chat(tenantId, request)
  │
  ├─ 1. 取候选链（本地 Caffeine 缓存，60s 刷新 + 变更事件驱动失效）
  │
  ├─ 2. 过滤掉 FUSE_OPEN 的
  │
  ├─ 3. 取第一个 → 发起调用（timeout 取自表配置）
  │
  ├─ 4. 失败？
  │     ├─ 可重试错误（429/5xx/超时）→ retry_count 内重试同一模型
  │     └─ 仍失败 → 记 DEGRADED → 换下一个候选 → 回到 3
  │
  ├─ 5. 全部候选失败 → 抛出 AiUnavailableException（**绝不静默降级为假成功**）
  │
  └─ 6. 成功 → 异步写 ai_call_log（model, tokens, latency, attempt_chain）
```

### 6.5 关键实现约束

| 约束 | 说明 |
| :--- | :--- |
| **配置获取走 Dubbo** | `ai_model_config` 在 `ry-cloud`，新增 `RemoteModelConfigService`（ruoyi-api-system），禁止 ai 服务直连 system 库 |
| **api_key 不落日志** | 日志、异常栈、审计表里都要脱敏（只留 `sk-3a08***`） |
| **租户级缓存** | key = `tenantId`，避免每次调用打一次 Dubbo |
| **禁止失败静默** | 全链路失败必须抛异常让上层感知——参考 `DefaultMarkAiServiceImpl` 的写法（明确返回"未接入"而不是假装成功），这个设计哲学要延续 |
| **切换可观测** | 每次切换打 WARN 日志 + 计入 `ai_call_log.attempt_chain`，运维要能看见"主模型挂了多久" |
| **单模型手动指定** | 支持 `modelCode` 显式指定（调试/AB 测试用），指定时不走 failover 链但走熔断 |

### 6.6 需要新增的表（脚本放 `script/sql/update/`，不直接执行）

```sql
-- AI 调用日志：成本核算 + 排障 + 审计
CREATE TABLE IF NOT EXISTS ai_call_log (
  id            bigint       NOT NULL COMMENT '主键',
  tenant_id     varchar(20)  DEFAULT '' COMMENT '租户',
  biz_type      varchar(32)  DEFAULT '' COMMENT '业务类型 mark/question/paper/...',
  skill_code    varchar(64)  DEFAULT '' COMMENT '技能编码',
  biz_id        varchar(64)  DEFAULT '' COMMENT '业务ID（题目/答卷）',
  model_name    varchar(100) DEFAULT '' COMMENT '实际使用的模型',
  attempt_chain varchar(500) DEFAULT '' COMMENT '尝试链 DeepSeek>Qwen',
  prompt_tokens int          DEFAULT 0,
  completion_tokens int      DEFAULT 0,
  latency_ms    int          DEFAULT 0,
  success       char(1)      DEFAULT '0',
  error_msg     varchar(500) DEFAULT '',
  create_time   datetime     DEFAULT NULL,
  PRIMARY KEY (id), KEY idx_tenant_time (tenant_id, create_time)
) ENGINE=InnoDB COMMENT='AI调用日志';

-- Prompt 模板：改 Prompt 不发版，支持租户级覆写
CREATE TABLE IF NOT EXISTS ai_prompt_template (
  id          bigint      NOT NULL,
  tenant_id   varchar(20) DEFAULT '000000' COMMENT '000000=系统默认',
  skill_code  varchar(64) NOT NULL,
  version     varchar(20) DEFAULT 'v1',
  system_msg  text,
  user_tpl    text COMMENT '含 {{占位符}}',
  params      varchar(1000) DEFAULT '' COMMENT 'temperature/maxTokens等',
  status      char(1)     DEFAULT '0',
  del_flag    char(1)     DEFAULT '0',
  create_time datetime    DEFAULT NULL,
  PRIMARY KEY (id), UNIQUE KEY uk_tpl (tenant_id, skill_code, version)
) ENGINE=InnoDB COMMENT='AI提示词模板';

-- Agent 任务：长任务追踪与断点
CREATE TABLE IF NOT EXISTS ai_agent_task (
  id          bigint      NOT NULL,
  tenant_id   varchar(20) DEFAULT '',
  task_type   varchar(64) DEFAULT '' COMMENT 'paper_gen/question_audit/...',
  status      varchar(20) DEFAULT 'RUNNING' COMMENT 'RUNNING/SUCCESS/FAILED/WAIT_HUMAN',
  progress    int         DEFAULT 0,
  input_json  json,
  output_json json,
  error_msg   varchar(1000) DEFAULT '',
  create_by   varchar(64) DEFAULT '',
  create_time datetime    DEFAULT NULL,
  PRIMARY KEY (id), KEY idx_status (status)
) ENGINE=InnoDB COMMENT='Agent任务';
```

**需要扩字段的（幂等脚本）**：
```sql
ALTER TABLE question
  ADD COLUMN knowledge_points varchar(500) DEFAULT NULL COMMENT '知识点JSON数组' AFTER analysis,
  ADD COLUMN source_type      varchar(20)  DEFAULT 'manual' COMMENT 'manual/ai/import' AFTER knowledge_points,
  ADD COLUMN ai_generated     char(1)      DEFAULT '1' COMMENT '0AI生成 1非AI' AFTER source_type,
  ADD COLUMN quality_score    decimal(4,2) DEFAULT NULL COMMENT '质检分' AFTER ai_generated,
  ADD COLUMN review_status    varchar(20)  DEFAULT 'none' COMMENT 'none/passed/rejected' AFTER quality_score;
```

---

## 七、FDE 视角：怎么把这套东西真正交付出去

> FDE（Forward Deployed Engineer）不是"做功能的人"，是"让客户用起来并产生可衡量价值的人"。
> 下面按 FDE 的真实工作流来拆。

### 7.1 第一步：先别写代码，去现场看老师怎么干活

FDE 的第一原则：**Embedding before building**。在没有观察过以下三个真实场景之前，不要动手：

| 要观察的对象 | 想搞清楚的问题 |
| :--- | :--- |
| 老师改主观题 | 一份答卷看多久？什么情况下会犹豫？有没有"看字迹给分"？ |
| 教务组一场考 | 出题到发布要几天？瓶颈是出题还是审批还是录系统？ |
| 考后复盘 | 现在有没有复盘？靠 Excel 还是靠拍脑袋？ |

**判断依据**：如果老师批一份论述题平均 90 秒，一场 200 人 × 5 道主观题 = 25 小时纯批改。
那么 AI 预评哪怕只省一半，也是**12.5 小时/场**的硬价值——这就是要写进方案第一页的数字。

### 7.2 第二步：价值排序（按 ROI，不是按技术酷炫度）

| 优先级 | 交付物 | 客户可感知价值 | 技术难度 | 依赖阻塞 |
| :---: | :--- | :--- | :---: | :--- |
| **P0** | AI 主观题预评 | 批改耗时 ↓50%+ | 低（**表字段和接口都现成**） | 仅模型网关 |
| **P0** | 模型网关 + 主备切换 | 系统不会因为模型挂了就不可用 | 中 | 无 |
| **P1** | 文档/知识点批量出题 | 出题从"天"变"小时" | 中 | RAG（一期可免向量库） |
| **P1** | 试题质检 + 知识点打标 | 题库质量可视化 | 中 | `question` 扩字段 |
| **P2** | 智能组卷 | 组卷从 2 小时到 10 分钟 | 高 | 依赖打标完成 |
| **P2** | 学情诊断 + 个性化推题 | 考生侧留存与口碑 | 中 | `wrong_question` 已有 |
| **P3** | 考务 Copilot / 数据助手 | 管理效率 | 高 | 依赖工具层完备 |
| **P3** | 监考分析 | 争议处理有据可依 | 中 | 事件数据积累 |

**FDE 建议：P0 两周内上线，让老师先用起来。** 一个能真的省下批改时间的按钮，比一份漂亮的架构图有用一万倍。

### 7.3 第三步：信任是第一产品需求（Trust & Safety）

教育评分是**高风险决策**，AI 出错的代价是学生投诉、成绩申诉、甚至舆情。FDE 必须内置这些：

| 机制 | 落地方式 |
| :--- | :--- |
| **AI 只给建议分，不给定分** | `ai_score` 与 `score` 分离（**表已经这么设计了**），最终分必须人工确认或批量采纳 |
| **分歧可见** | 界面上直接标出"AI 给 7 分，你给 5 分"，并展示 AI 理由 |
| **置信度门槛** | AI 低置信度的题不打建议分，只标"需人工"——**宁可不给，不要乱给** |
| **一键否决** | 老师能一键清空本场所有 AI 分，回退到纯人工 |
| **全程留痕** | `exam_mark_log` 已存在，AI 预评也要记一条，注明 `mark_type=ai` |
| **申诉可解释** | 学生问"为什么扣这 2 分"时，AI 理由必须是可展示给学生的自然语言 |

> **最危险的失败模式不是 AI 判错，而是老师无脑全盘采纳。**
> 所以 UI 上要做"反惰性设计"：批量采纳要二次确认，且默认只采纳"AI 与人工一致"的题。

### 7.4 第四步：成本与单位经济

FDE 要对客户的钱负责。按 DeepSeek 类模型估算：

| 场景 | 单次 token | 单场考试量 | 单场成本量级 |
| :--- | :--- | :--- | :--- |
| 主观题预评 | ~1.2k | 200 人 × 5 题 = 1000 次 | 约 ¥1-3 元 |
| 出题（Reflection） | ~3k | 100 题 | 约 ¥0.5 元 |
| 组卷（ReAct 12 步） | ~15k | 1 次 | 约 ¥0.2 元 |

**结论：AI 阅卷的单位成本极低（元级），但节省的是十小时级人力——ROI 是压倒性的。**
这正是它必须排 P0 的原因。同时要在管理后台做**租户级 token 配额与预算告警**，避免失控。

### 7.5 第五步：落地风险清单（FDE 必须提前和客户说清楚）

| 风险 | 表现 | 缓解 |
| :--- | :--- | :--- |
| **老师不信任** | "机器怎么可能懂我的课" | 先跑"影子模式"：AI 打分但不显示，跑两周后拿 AI 与人工的一致率数据说话 |
| **学生质疑公平** | "为什么他比我高" | 公开评分要点 + 申诉通道 + 人工终裁 |
| **数据出境/合规** | 考试内容、学生作答发给第三方模型 | 合同与隐私声明；敏感单位支持私有化（本地 ollama / vLLM），**模型网关要预留本地模型接入能力** |
| **模型供应商单点** | DeepSeek 挂了 | 主备切换（第六节）+ 至少配两个不同厂商 |
| **Prompt 漂移** | 换模型后效果变差 | Golden Set 回归测试，模型切换前必须跑 |
| **题库污染** | AI 生成的错题混入正式题库 | `ai_generated` 标记 + 强制人工审核态，未审过的题不能进正式考试 |
| **幻觉** | 生成的解析是错的 | Reflection 自检 + 人工抽检 + 来源引用 |

### 7.6 第六步：推荐的交付节奏

```
第 1-2 周   P0：模型网关（主备+熔断+日志） + AI 主观题预评上线
            → 找 1 位老师真实用一场考试，拿到"省了多少时间"的真实数据

第 3-4 周   P0 加固：评分校准、批量采纳、置信度门槛、影子模式报告
            → 用一致率数据说服第二批老师

第 5-8 周   P1：文档出题 + 试题质检 + 知识点打标
            → 题库从"能用"变"好用"

第 9-12 周  P2：智能组卷 + 学情诊断
            → 打通"命题-考试-阅卷-学情"闭环

持续        Guardrail / 成本看板 / Golden Set 回归 / 模型切换演练
```

**FDE 的金句：不要追求一次交付完美架构，要追求第一周就让一个真实用户省下一小时。**

---

## 八、结论速览

1. **起跑线极好**：`MarkAiService` 桩、`exam_mark_item.ai_*` 字段、`ai_model_config` 表、空置的 `ruoyi-exam-ai` 模块——前人把接口都留好了，AI 阅卷是**改动最小、价值最高**的第一刀。

2. **架构上**：L1 模型 → L2 网关（主备/熔断）→ L3 工具·Skill·记忆·RAG → L4 编排 → L5 接入。工具层直接复用 11 个 `Remote*Service`，不要重写业务。

3. **模式选择上**：
   - **Tool-Augmented 是地基**，全场景必备；
   - **Reflection 性价比最高**，命题和阅卷必须加；
   - **RAG 主攻命题与答疑**，一期不必上向量库；
   - **ReAct 主攻组卷与诊断**，**Plan-and-Execute 主攻长流程**，两者混合最实用；
   - **Multi-Agent 只在"错不起"时用**（命题流水线、阅卷仲裁）；
   - **Memory-Augmented 主攻学情**，`wrong_question` 是现成载体；
   - **Autonomous Loop 只放后台巡检**，碰分数和发布的动作一律 HITL。

4. **Skill 化**：先做 9 个 P0 Skill（出题/干扰项/质检/改写/评分/代码判分/校准/打标/解析），每个配 Golden Set 和单测。

5. **模型主备**：`priority ASC` 取主，失败按 `retry_count` 重试后切下一个，配熔断状态机，全部失败必须抛异常而非静默降级，全过程写 `ai_call_log`。

6. **FDE 落地**：先去现场量化痛点 → P0 两周上线 AI 预评 → 影子模式建立信任 → 单位经济算给客户听 → 严守"AI 给建议、人做决定"的红线。
