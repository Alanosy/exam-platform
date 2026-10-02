/**
 * 首页统计类型
 *
 * 服务端：ruoyi-exam-stat → StatHomeController（/home/overview）
 * 网关前缀：/stat
 *
 * 约定：Long 字段走 BigNumberSerializer，BigDecimal 走 ToStringSerializer，
 * 所以「比例 / 分数」这类值到前端可能是字符串。这里统一声明成联合类型，
 * 展示时用 num() 兜一层，避免 NaN 出现在卡片上。
 */

/** 一天的交卷趋势 */
export interface TrendVO {
  /** 日期 yyyy-MM-dd */
  date: string;
  /** 当天交卷数 */
  submitCount: number | string;
  /** 当天及格数 */
  passCount: number | string;
  /** 当天平均分 */
  avgScore: number | string;
}

/** 今日考试安排 */
export interface HomeExamVO {
  /** 考试ID */
  examId: string | number;
  /** 考试名称 */
  examName: string;
  /** 考试类型 1正式考试 / 2练习考试 */
  examType: string;
  /** 开始时间 */
  startTime: string;
  /** 结束时间 */
  endTime: string;
  /** 考试状态 not_start / ongoing / finished / archived */
  status: string;
}

/** 考试热度榜 */
export interface HomeExamRankVO {
  /** 考试ID */
  examId: string | number;
  /** 考试名称，模板被删时是「已删除的考试」 */
  examName: string;
  /** 交卷人数 */
  submitCount: number | string;
  /** 及格人数 */
  passCount: number | string;
  /** 及格率（百分数） */
  passRate: number | string;
  /** 平均分 */
  avgScore: number | string;
}

/** 首页总览（管理 / 老师视角） */
export interface HomeStatVO {
  /** 考试总数 */
  examTotal: number | string;
  /** 进行中 */
  examOngoing: number | string;
  /** 今天有安排的场次 */
  examToday: number | string;
  /** 已结束 */
  examFinished: number | string;
  /** 累计答卷 */
  recordTotal: number | string;
  /** 正在答题 */
  answering: number | string;
  /** 今日交卷 */
  todaySubmit: number | string;
  /** 参考人数（去重） */
  examineeCount: number | string;
  /** 及格率（百分数） */
  passRate: number | string;
  /** 平均分 */
  avgScore: number | string;
  /** 近7天趋势 */
  trend: TrendVO[];
  /** 今日考试安排 */
  todayExams: HomeExamVO[];
  /** 考试热度榜 */
  examRank: HomeExamRankVO[];
}

/* ============================================================================
 * 以下为「考试统计」模块类型
 * 服务端：ruoyi-exam-stat → StatController（/stat/**）
 * ========================================================================== */

/** 统计数字统一用 string：后端 BigDecimal 经 ToStringSerializer 序列化后是字符串，
 *  ID 是雪花 Long 也按字符串透传，禁止 Number() 转换（19 位会丢精度）。 */

/** 考试统计行 / 概览 */
export interface StatExamRowVO {
  examId: string;
  paperId?: string;
  examName?: string;
  /** not_start / ongoing / finished / archived */
  examStatus?: string;
  examType?: string;
  startTime?: string;
  endTime?: string;
  /** 应考人数 */
  invitedCount?: number;
  /** 已交卷 */
  submittedCount?: number;
  /** 已入统：真正进统计的人数 */
  countedCount?: number;
  /** 待阅卷：>0 时整行标橙 */
  pendingMarkCount?: number;
  excludedCount?: number;
  attendanceRate?: string;
  fullScore?: string;
  passScore?: string;
  maxScore?: string;
  minScore?: string;
  avgScore?: string;
  medianScore?: string;
  stdDev?: string;
  passCount?: number;
  passRate?: string;
  excellentCount?: number;
  excellentRate?: string;
  avgUsedSeconds?: number;
  avgObjectiveScore?: string;
  avgSubjectiveScore?: string;
  difficulty?: string;
  discrimination?: string;
  hasSubjective?: string;
  /** 客观题部分得分开关 0必须全对 1部分得分 */
  partialScore?: string;
  partialScoreRate?: number;
  calcStatus?: string;
  calcVersion?: number;
  calcTime?: string;
  /** 数据可能不是最新 */
  stale?: boolean;
}

/** 分数段 */
export interface StatSegmentVO {
  segmentLabel: string;
  segmentMin?: string;
  segmentMax?: string;
  personCount?: number;
  sort?: number;
  rate?: string;
  cumulativeRate?: string;
}

/** 阅卷进度 */
export interface StatMarkProgressVO {
  recordId?: string;
  account?: string;
  userName?: string;
  questionCount?: number;
  markedCount?: number;
  status?: string;
  markerName?: string;
}

/** 单场考试详情 */
export interface StatOverviewVO {
  summary?: StatExamRowVO;
  segments?: StatSegmentVO[];
  pendingMarks?: StatMarkProgressVO[];
  partialScore?: string;
  partialScoreRate?: number;
}

/** 大盘 KPI */
export interface StatKpiVO {
  examCount?: number;
  countedCount?: number;
  avgPassRate?: string;
  avgScore?: string;
  pendingMarkCount?: number;
  pendingMarkExamCount?: number;
  avgUsedSeconds?: number;
}

/** 大盘概览 */
export interface StatDashboardVO {
  kpi?: StatKpiVO;
  scoreDistribution?: StatSegmentVO[];
  topPassRate?: StatExamRowVO[];
  rows?: {
    rows: StatExamRowVO[];
    total: number;
  };
}

/** 考生成绩行 */
export interface StatUserVO {
  id?: string;
  examId?: string;
  userId?: string;
  recordId?: string;
  attemptNo?: number;
  account?: string;
  userName?: string;
  deptName?: string;
  totalScore?: string;
  objectiveScore?: string;
  subjectiveScore?: string;
  correctCount?: number;
  wrongCount?: number;
  blankCount?: number;
  passed?: number;
  rankNo?: number;
  usedSeconds?: number;
  submitTime?: string;
  /** COUNTED / PENDING_MARK / EXCLUDED */
  statStatus?: string;
  beatRate?: string;
  fullScore?: string;
}

/** 选项分布 */
export interface StatOptionVO {
  optionKey: string;
  optionContent?: string;
  selectCount?: number;
  selectRate?: string;
  isCorrect?: string;
}

/** 试题分析行 */
export interface StatQuestionVO {
  examId?: string;
  questionId?: string;
  sort?: number;
  questionType?: string;
  /** objective / subjective */
  questionCategory?: string;
  difficulty?: string;
  title?: string;
  fullScore?: string;
  answerCount?: number;
  blankCount?: number;
  correctCount?: number;
  correctRate?: string;
  /** 部分正确人数，不能算进答错 */
  partialCount?: number;
  wrongCount?: number;
  avgScore?: string;
  scoreRate?: string;
  maxScore?: string;
  minScore?: string;
  zeroCount?: number;
  fullCount?: number;
  difficultyIndex?: string;
  discrimination?: string;
  options?: StatOptionVO[];
  suggestion?: string;
}

/** 知识点行 */
export interface StatKnowledgeVO {
  knowledgeId?: string;
  knowledgeName?: string;
  knowledgePath?: string;
  questionCount?: number;
  fullScore?: string;
  avgScore?: string;
  scoreRate?: string;
  wrongCount?: number;
  wrongRate?: string;
  mastery?: string;
  /** good / normal / weak */
  weakLevel?: string;
  affectedUserCount?: number;
}

/** 答卷明细里的一道题 */
export interface StatAnswerItemVO {
  questionId?: string;
  sort?: number;
  questionType?: string;
  questionCategory?: string;
  difficulty?: string;
  title?: string;
  options?: StatOptionVO[];
  answerContent?: string;
  answerText?: string;
  standardAnswerText?: string;
  fullScore?: string;
  score?: string;
  /** 1正确 2错误 3部分正确 0未判 */
  correct?: number;
  /** 正确 / 部分正确 / 错误 / 未答 / 待阅 */
  result?: string;
  markType?: string;
  markerName?: string;
  markComment?: string;
  analysis?: string;
}

/** 历次参考 */
export interface StatAttemptVO {
  recordId?: string;
  attemptNo?: number;
  totalScore?: string;
  statStatus?: string;
}

/** 考生答卷明细 */
export interface StatAnswerVO {
  recordId?: string;
  examId?: string;
  userId?: string;
  attemptNo?: number;
  account?: string;
  userName?: string;
  deptName?: string;
  totalScore?: string;
  objectiveScore?: string;
  subjectiveScore?: string;
  passScore?: string;
  fullScore?: string;
  passed?: number;
  rankNo?: number;
  countedCount?: number;
  beatRate?: string;
  usedSeconds?: number;
  avgUsedSeconds?: number;
  statStatus?: string;
  attempts?: StatAttemptVO[];
  items?: StatAnswerItemVO[];
}

/** 大盘 / 列表筛选 */
export interface StatExamQuery {
  keyword?: string;
  examType?: string;
  examStatus?: string;
  beginTime?: string;
  endTime?: string;
  onlyPendingMark?: boolean;
  onlyMine?: boolean;
  pageNum?: number;
  pageSize?: number;
}

/** 考生筛选 */
export interface StatUserQuery {
  examId?: string;
  keyword?: string;
  passed?: number;
  minScore?: string;
  maxScore?: string;
  statStatus?: string;
  pageNum?: number;
  pageSize?: number;
}

/** 试题筛选 */
export interface StatQuestionQuery {
  examId?: string;
  questionType?: string;
  difficulty?: string;
  questionCategory?: string;
  onlyAbnormal?: boolean;
  keyword?: string;
  pageNum?: number;
  pageSize?: number;
}
