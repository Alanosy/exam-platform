/** 考试中心：我的考试卡片 */
export interface ExamCenterVO {
  /** 考试ID */
  examId: string | number;

  /** 考试名称 */
  examName: string;

  /** 考试类型 1正式考试 / 2练习考试 */
  examType: string;

  /** 考试描述 */
  examDesc: string;

  /** 开始时间 */
  startTime: string;

  /** 结束时间 */
  endTime: string;

  /** 限时（分钟），0 不限时 */
  duration: number;

  /** 考试状态 not_start / ongoing / finished / archived */
  examStatus: string;

  /** 我的状态 not_start未开始 / pending待考试 / answering答题中 / submitted已交卷 / ended已结束 / late迟到 / blocked不可参加 */
  myStatus: string;

  /** 不可参加时的原因 */
  tip: string;

  /** 进行中或最近一次答卷ID */
  recordId?: string | number;

  /** 已参加次数 */
  attemptCount: number;

  /** 最近一次成绩 */
  totalScore?: number;

  /** 及格分 */
  passScore?: number;

  /** 是否及格 */
  passed?: boolean;

  /** 是否可以开始 / 继续考试 */
  canStart: boolean;

  /** 是否是我创建的考试（创建人免邀请直接进入考试中心） */
  owner?: boolean;
}

/** 答题页的选项 */
export interface ExamOptionVO {
  /** 选项标识 A/B/C/D */
  optionKey: string;

  /** 选项内容富文本 */
  optionContent: string;
}

/** 答题页的题目 */
export interface ExamQuestionVO {
  /** 试题ID */
  questionId: string | number;

  /** 题型 SINGLE/MULTIPLE/JUDGE/BLANK/SHORT_ANSWER/ESSAY/CODE/UPLOAD_FILE/MATCH */
  questionType: string;

  /** 题干富文本 */
  title: string;

  /** 本题分值 */
  score: number;

  /** 题号，从 1 开始 */
  sort: number;

  /** 选项 */
  options?: ExamOptionVO[];

  /** 我已作答的内容（JSON 字符串） */
  myAnswer?: string;
}

/** 答题页数据 */
export interface ExamPaperVO {
  /** 答卷记录ID */
  recordId: string | number;

  /** 考试ID */
  examId: string | number;

  /** 考试名称 */
  examName: string;

  /** 试卷名称 */
  paperName: string;

  /** 试卷总分 */
  totalScore: number;

  /** 题目列表 */
  questions: ExamQuestionVO[];

  /** 剩余秒数，-1 表示不限时 */
  remainingSeconds: number;

  /** 开考时间（毫秒时间戳） */
  startTime: string;
}

/** 考试记录列表：一次答卷一行 */
export interface ExamRecordVO {
  /** 答卷记录ID */
  recordId: string | number;

  /** 考试ID */
  examId: string | number;

  /** 考试名称 */
  examName: string;

  /** 考试类型 1正式考试 / 2练习考试 */
  examType: string;

  /** 试卷ID */
  paperId: string | number;

  /** 试卷名称 */
  paperName: string;

  /** 第几次参加，从 1 开始 */
  attemptNo: number;

  /** answering答题中 / submitted已交卷 / expired超时作废 */
  status: string;

  /** 开考时间 */
  startTime: string;

  /** 交卷时间 */
  submitTime: string;

  /** 用时（秒） */
  usedSeconds: number;

  /** 限时（分钟），0 不限时 */
  durationMinutes: number;

  /** 题目总数 */
  questionCount: number;

  /** 已作答题目数 */
  answeredCount: number;

  /** 判对题数 */
  correctCount: number;

  /** 判错题数 */
  wrongCount: number;

  /** 客观题得分 */
  objectiveScore: number;

  /** 主观题得分 */
  subjectiveScore: number;

  /** 我的总分 */
  totalScore: number;

  /** 试卷总分 */
  paperTotalScore: number;

  /** 及格分 */
  passScore: number;

  /** 是否及格 */
  passed: boolean;

  /** 是否超时自动交卷 */
  autoSubmit: boolean;

  /** 该考试当前是否允许看答案与解析 */
  showAnswer: boolean;
}

/** 考试记录查询条件 */
export interface ExamRecordQuery {
  /** 考试ID */
  examId?: string | number;

  /** 状态 answering / submitted / expired */
  status?: string;

  /** 考试类型 1正式考试 / 2练习考试 */
  examType?: string;

  /** 是否及格 */
  passed?: boolean;

  pageNum?: number;
  pageSize?: number;
}

/** 成绩详情里的单题结果 */
export interface ExamResultQuestionVO {
  questionId: string | number;
  questionType: string;
  title: string;
  sort: number;
  score: number;
  gainedScore: number;
  /** 0未判 1正确 2错误 */
  correct: number;
  myAnswer?: string;
  /** 我的作答，人话版 */
  myAnswerText?: string;
  standardAnswer?: string;
  /** 参考答案，人话版，仅 showAnswer=true 时有值 */
  standardAnswerText?: string;
  analysis?: string;
  /** 选项，客观题才有 */
  options?: ExamOptionVO[];
}

/** 交卷结果 / 成绩 */
export interface ExamResultVO {
  recordId: string | number;
  examId: string | number;
  examName: string;
  paperId: string | number;
  paperName: string;
  /** answering / submitted / expired */
  status: string;
  /** 第几次参加，从 1 开始 */
  attemptNo: number;
  startTime: string;
  submitTime: string;
  /** 限时（分钟），0 不限时 */
  durationMinutes: number;
  questionCount: number;
  answeredCount: number;
  correctCount: number;
  wrongCount: number;
  objectiveScore: number;
  subjectiveScore: number;
  totalScore: number;
  paperTotalScore: number;
  passScore: number;
  passed: boolean;
  usedSeconds: number;
  autoSubmit: boolean;
  /** 是否展示答案与解析 */
  showAnswer: boolean;
  questions: ExamResultQuestionVO[];
}

/** 保存单题作答 */
export interface AnswerSaveForm {
  questionId: string | number;
  answerContent: string;
}
