/**
 * AI 能力相关类型
 *
 * 服务端：ruoyi-exam-ai → AiController（/ai）
 * 底层：Python agent（ruoyi-exam-agent），Java 侧只做转发
 *
 * 约定：所有 ID 一律字符串透传（雪花 ID 19 位，超过 JS 安全整数范围，
 * 用 Number() 转换会丢精度）
 */

/**
 * AI 是否可用
 *
 * 后端 /ai/enabled 必须返回 { enabled: boolean } 而不是裸布尔，
 * 页面读的是 data.enabled，返回 true/false 时这里恒为 undefined（表现为「AI 服务不可用」）
 */
export interface AiEnabledVO {
  enabled: boolean;
}

/** 模型配置（脱敏，不含密钥） */
export interface AiModelVO {
  /** 模型编码 */
  code: string;
  /** 模型名称 */
  name: string;
  /** 供应商 / 类型 */
  type: string;
  /** 配置来源 mysql / env / mock */
  source: string;
  /** 优先级，越小越优先 */
  priority: number;
  /** 熔断状态 HEALTHY / DEGRADED / FUSE_OPEN / HALF_OPEN */
  state: string;
  /** 是否当前主用 */
  primary: boolean;
}

/** AI 选项 */
export interface AiOptionVO {
  key: string;
  content: string;
}

/** 评分命中的要点 */
export interface AiMatchPointVO {
  /** 要点描述 */
  point: string;
  /** 是否命中 */
  got: boolean;
  /** 该要点得分 */
  score: number;
}

/** 主观题 AI 评分入参 */
export interface AiMarkScoreForm {
  questionId?: string;
  questionType?: string;
  title?: string;
  standardAnswer?: string;
  rubric?: string;
  analysis?: string;
  answerText?: string;
  fullScore?: number;
}

/** 主观题 AI 评分结果（建议分，不是最终分） */
export interface AiMarkScoreVO {
  success: boolean;
  score?: number;
  reason?: string;
  comment?: string;
  /** 置信度 0-1，低于 0.6 建议人工复核 */
  confidence?: number;
  needHuman?: boolean;
  matchedPoints?: AiMatchPointVO[];
  model?: string;
  message?: string;
}

/** AI 出题入参 */
export interface AiQuestionGenForm {
  questionType: string;
  difficulty: string;
  knowledgePoints?: string[];
  count?: number;
  score?: number;
  extra?: string;
  ragContext?: string;
  withAudit?: boolean;
}

/** AI 生成的题目 */
export interface AiQuestionGenVO {
  questionType: string;
  stem: string;
  options?: AiOptionVO[];
  /** 字符串形式的 JSON，结构与 question.answer 一致，落库前不要加工 */
  answer?: string;
  analysis?: string;
  knowledgePoints?: string[];
  difficulty?: string;
  score?: number;
}

/** 质检发现的问题 */
export interface AiIssueVO {
  /** fatal / major / minor */
  level: string;
  type?: string;
  detail?: string;
  suggestion?: string;
}

/** 试题质检结论 */
export interface AiQuestionAuditVO {
  success: boolean;
  passed?: boolean;
  qualityScore?: number;
  issues?: AiIssueVO[];
  summary?: string;
  model?: string;
  message?: string;
}

/** 试卷审查入参里的题目摘要 */
export interface AiPaperQuestionItem {
  questionId?: string;
  questionType?: string;
  stem?: string;
  difficulty?: string;
  knowledgePoints?: string[];
  score?: number;
}

/** 试卷审查入参 */
export interface AiPaperReviewForm {
  paperId?: string;
  title?: string;
  duration?: number;
  totalScore?: number;
  passScore?: number;
  questions?: AiPaperQuestionItem[];
  focus?: string;
}

/** 知识点覆盖 */
export interface AiKnowledgeCoverageVO {
  point: string;
  count?: number;
  ratio?: number;
}

/** 试卷审查结论 */
export interface AiPaperReviewVO {
  success: boolean;
  questionCount?: number;
  difficultyDistribution?: string;
  knowledgeCoverage?: AiKnowledgeCoverageVO[];
  estimatedMinutes?: number;
  issues?: AiIssueVO[];
  suggestions?: string[];
  verdict?: string;
  model?: string;
  message?: string;
}

/** 错题摘要 */
export interface AiWrongItem {
  questionId?: string;
  questionType?: string;
  stem?: string;
  knowledgePoints?: string[];
  answerText?: string;
  standardAnswer?: string;
  wrongCount?: number;
}

/** 错题归因入参 */
export interface AiDiagnoseForm {
  userId?: string;
  wrongItems?: AiWrongItem[];
  mastered?: string[];
}

/** 薄弱知识点 */
export interface AiWeakPointVO {
  knowledgePoint: string;
  wrongCount?: number;
  /** 掌握度 0-1，越低越薄弱 */
  mastery?: number;
  errorType?: string;
  evidence?: string;
}

/** 错题归因结论 */
export interface AiDiagnoseVO {
  success: boolean;
  weakPoints?: AiWeakPointVO[];
  advice?: string;
  priority?: string[];
  model?: string;
  message?: string;
}

/** 通用 Skill 执行入参 */
export interface AiSkillRunForm {
  skillCode: string;
  input?: Record<string, any>;
  modelCode?: string;
}

/** 通用 Skill 执行结果 */
export interface AiSkillRunVO {
  success: boolean;
  skillCode?: string;
  data?: any;
  model?: string;
  attemptChain?: string[];
  latencyMs?: number;
  message?: string;
}

/* ------------------------------------------------------------------ *
 * 对话式 Agent（/ai/chat）
 * ------------------------------------------------------------------ */

/**
 * 运行流程的一个步骤
 *
 * 前端按 type 渲染不同图标：think 思考 / tool 工具 / skill 技能 /
 * ask 等待补充 / write 写入 / done 完成 / error 出错
 */
export interface AiChatTraceVO {
  id: string;
  type: string;
  title: string;
  detail?: string;
  /** ok / error / waiting */
  status?: string;
  /** 工具 code 或技能 code */
  ref?: string;
  latencyMs?: number;
  /** 关键结果摘要 */
  preview?: string[];
}

/** 中断卡候选项，value 一律字符串（雪花 ID 走数字会丢精度） */
export interface AiChatOptionVO {
  label: string;
  value: string;
}

/** 中断卡字段 */
export interface AiChatFieldVO {
  key: string;
  label: string;
  /** text / number / select / multi / switch */
  type: string;
  options?: AiChatOptionVO[];
  value?: any;
  required?: boolean;
  placeholder?: string;
  tip?: string;
}

/**
 * 中断卡：Agent 缺信息时让人补
 *
 * kind=form 渲染表单 / kind=choice 渲染选项 / kind=confirm 渲染确认按钮
 */
export interface AiChatAskVO {
  kind: string;
  title: string;
  desc?: string;
  fields?: AiChatFieldVO[];
  submitText?: string;
  cancelText?: string;
}

/** 一轮对话结果 */
export interface AiChatVO {
  sessionId: string;
  /** Markdown 正文 */
  reply: string;
  trace?: AiChatTraceVO[];
  /** 不为空表示「我在等你补充信息」 */
  ask?: AiChatAskVO | null;
  intent?: string;
  data?: Record<string, any>;
}

/** 一轮对话入参 */
export interface AiChatForm {
  sessionId?: string;
  message?: string;
  /** 中断卡答案 {key: value} */
  answers?: Record<string, any>;
  modelCode?: string;
  /** 会话设置：contextRounds / confirmWrite / planner / modelCode */
  options?: Record<string, any>;
}

/** 历史会话（列表只带摘要，详情才带消息） */
export interface AiChatSessionVO {
  id: string;
  title: string;
  intent?: string;
  messageCount?: number;
  updatedAt?: number;
  messages?: { role: string; content: string }[];
}

/**
 * 聊天窗设置
 *
 * 存在 localStorage：这是「个人偏好」而不是业务数据，
 * 没必要为它单独开一张表，换个浏览器重置一次也能接受。
 */
export interface AiChatSettings {
  /** 带多少轮历史进上下文（1-20） */
  contextRounds: number;
  /** 写操作是否必须先确认 */
  confirmWrite: boolean;
  /** 开放域问题是否先规划再执行 */
  planner: boolean;
  /** 模型编码，空表示默认 */
  modelCode: string;
}
