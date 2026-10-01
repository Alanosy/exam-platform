/**
 * 错题本相关类型
 *
 * 服务端：ruoyi-exam-practice → WrongQuestionController（/wrong）
 * 网关前缀：/practice
 *
 * 约定：所有 ID 一律字符串透传（雪花 ID 19 位，超过 JS 安全整数范围，
 * 用 Number() 转换会丢精度，例如 2105488649030184961 → 2105488649030185000）
 */

/** 错题本总览统计 */
export interface WrongOverviewVO {
  /** 错题总数（不含已忽略） */
  totalCount: number;
  /** 未掌握 */
  notMasterCount: number;
  /** 已掌握 */
  masteredCount: number;
  /** 已忽略 */
  ignoredCount: number;
  /** 今日新增 */
  todayCount: number;
  /** 近七天新增 */
  weekCount: number;
  /** 题型分布：题型编码 → 数量 */
  typeDistribution: Record<string, number>;
  /** 难度分布：难度编码 → 数量 */
  difficultyDistribution: Record<string, number>;
}

/** 错题来源（某场考试 / 某份练习）统计 */
export interface WrongSourceVO {
  /** 来源类型 EXAM考试 / PAPER_PRACTICE试卷练习 */
  sourceType: string;
  /** 来源ID（考试ID / 练习ID），字符串避免精度丢失 */
  sourceId: string;
  /** 考试类型 1正式 / 2练习 */
  examType: string;
  /** 来源名称 */
  sourceName: string;
  /** 错题数 */
  wrongCount: number;
  /** 未掌握数 */
  notMasterCount: number;
  /** 已掌握数 */
  masteredCount: number;
  /** 最近答错时间 */
  lastWrongTime: string;
}

/** 来源分页查询条件 */
export interface WrongSourceQuery {
  /** 来源类型 */
  sourceType?: string;
  /** 考试类型 1正式 / 2练习 */
  examType?: string;
  /** 来源名称（模糊） */
  sourceName?: string;
  pageNum?: number;
  pageSize?: number;
}

/** 选项 */
export interface WrongOptionVO {
  /** 选项标识 A/B/C/D */
  optionKey: string;
  /** 选项内容富文本 */
  optionContent: string;
}

/** 错题条目 */
export interface WrongQuestionVO {
  /** 错题记录ID */
  id: string;
  /** 试题ID */
  questionId: string;
  /** 题型 SINGLE/MULTIPLE/JUDGE/BLANK/SHORT_ANSWER/ESSAY/CODE */
  questionType: string;
  /** 难度 easy/medium/hard */
  difficulty: string;
  /** 分值 */
  score: number;
  /** 题干富文本 */
  title: string;
  /** 选项 */
  options?: WrongOptionVO[];
  /** 解析富文本 */
  analysis: string;
  /** 标准答案（原始 JSON / 文本） */
  standardAnswer: string;
  /** 标准答案可读文本 */
  standardAnswerText: string;
  /** 来源类型 */
  sourceType: string;
  /** 来源ID */
  sourceId: string;
  /** 考试类型 */
  examType: string;
  /** 来源名称 */
  sourceName: string;
  /** 累计答错次数 */
  wrongCount: number;
  /** 累计答对次数 */
  rightCount: number;
  /** 掌握状态 NOT_MASTER / MASTERED / IGNORED */
  masterStatus: string;
  /** 我的笔记 */
  userNote: string;
  /** 最近答错时间 */
  lastWrongTime: string;
  /** 最近重做时间 */
  lastReviewTime: string;
  /** 进入错题本时间 */
  createTime: string;
}

/** 错题列表查询条件 */
export interface WrongQuestionQuery {
  /** 试题ID */
  questionId?: string;
  /** 来源类型 EXAM / PAPER_PRACTICE */
  sourceType?: string;
  /** 来源ID（考试ID / 练习ID），字符串透传 */
  sourceId?: string | number;
  /** 掌握状态 NOT_MASTER / MASTERED / IGNORED */
  masterStatus?: string;
  /** 是否包含已忽略的 */
  includeIgnored?: boolean;
  pageNum?: number;
  pageSize?: number;
}

/** 某道错题的重做历史 */
export interface WrongReviewRecordVO {
  id: string;
  /** 错题记录ID */
  userWrongId: string;
  /** 试题ID */
  questionId: string;
  /** 本次作答内容 */
  userAnswerText: string;
  /** 是否正确 */
  correct: boolean;
  /** 重做时间 */
  reviewTime: string;
}

/** 重做判分结果 */
export interface WrongReviewResultVO {
  /** 错题记录ID */
  wrongId: string;
  /** 试题ID */
  questionId: string;
  /** 是否正确；主观题为 null（不自动判分） */
  correct?: boolean;
  /** 我的作答可读文本 */
  myAnswerText: string;
  /** 正确答案可读文本 */
  standardAnswerText: string;
  /** 解析富文本 */
  analysis: string;
  /** 选项（判分后带回，便于高亮） */
  options?: WrongOptionVO[];
  /** 累计答对次数 */
  rightCount: number;
  /** 累计答错次数 */
  wrongCount: number;
  /** 判分后的掌握状态 */
  masterStatus: string;
  /** 本次是否自动转为已掌握 */
  autoMastered: boolean;
  /** 提示语 */
  message: string;
}

/** 重做提交表单 */
export interface WrongReviewForm {
  /** 作答内容：选择题为选项 JSON，填空为 blanks JSON，主观题为富文本 HTML */
  answerContent: string;
}

/** 笔记表单 */
export interface WrongNoteForm {
  /** 笔记内容 */
  userNote: string;
}
