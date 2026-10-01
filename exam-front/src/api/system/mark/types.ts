/**
 * 阅卷相关类型
 *
 * 服务端：ruoyi-exam-mark → MarkController（/mark）
 * 网关前缀：/mark
 *
 * 约定：所有 ID 一律字符串透传（雪花 ID 19 位，超过 JS 安全整数范围，
 * 用 Number() 转换会丢精度）
 */

/** 阅卷列表：按考试聚合 */
export interface MarkExamVO {
  /** 考试ID */
  examId: string;
  /** 考试名称 */
  examName: string;
  /** 试卷ID */
  paperId: string;
  /** 试卷名称 */
  paperName: string;
  /** 待阅答卷数 */
  pendingTaskCount: number;
  /** 已阅完答卷数 */
  finishedTaskCount: number;
  /** 答卷总数 */
  taskCount: number;
  /** 待阅题目数 */
  pendingItemCount: number;
  /** 已阅题目数 */
  markedItemCount: number;
  /** 主观题总数 */
  itemCount: number;
  /** 阅卷进度百分比 */
  progress: number;
}

/** 考试维度查询条件 */
export interface MarkExamQuery {
  /** 考试名称（模糊） */
  examName?: string;
  /** 试卷名称（模糊） */
  paperName?: string;
  /** 只看未阅完的 */
  onlyUnfinished?: boolean;
  pageNum?: number;
  pageSize?: number;
}

/** 某场考试下的一份答卷 */
export interface MarkTaskVO {
  /** 阅卷任务ID */
  taskId: string;
  /** 考试ID */
  examId: string;
  /** 考试名称 */
  examName: string;
  /** 试卷ID */
  paperId: string;
  /** 试卷名称 */
  paperName: string;
  /** 答卷ID */
  recordId: string;
  /** 考生ID */
  userId: string;
  /** 考生账号 */
  account: string;
  /** 考生姓名 */
  userName: string;
  /** 第几次参加 */
  attemptNo: number;
  /** 主观题总数 */
  questionCount: number;
  /** 已阅题数 */
  markedCount: number;
  /** 状态 pending待阅 / marking阅卷中 / finished已阅完 */
  status: string;
  /** 客观题得分 */
  objectiveScore: number;
  /** 主观题得分 */
  subjectiveScore: number;
  /** 总分 */
  totalScore: number;
  /** 及格分 */
  passScore: number;
  /** 是否及格 */
  passed: boolean;
  /** 交卷时间 */
  submitTime: string;
  /** 阅卷人 */
  markerName: string;
  /** 阅卷时间 */
  markTime: string;
}

/** 答卷维度查询条件 */
export interface MarkTaskQuery {
  /** 考试ID */
  examId?: string | number;
  /** 试卷ID */
  paperId?: string | number;
  /** 考生账号（模糊） */
  account?: string;
  /** 状态 pending / marking / finished */
  status?: string;
  pageNum?: number;
  pageSize?: number;
}

/** 阅卷页的选项 */
export interface MarkOptionVO {
  /** 选项标识 A/B/C/D */
  optionKey: string;
  /** 选项内容富文本 */
  optionContent: string;
}

/** 阅卷页的一道主观题 */
export interface MarkQuestionVO {
  /** 阅卷明细ID（打分时用这个） */
  itemId: string;
  /** 试题ID */
  questionId: string;
  /** 题型 */
  questionType: string;
  /** 题型名称 */
  questionTypeName: string;
  /** 难度 */
  difficulty: string;
  /** 题号 */
  sort: number;
  /** 题干富文本 */
  title: string;
  /** 选项 */
  options?: MarkOptionVO[];
  /** 本题满分 */
  fullScore: number;
  /** 考生作答原始内容（JSON） */
  answerContent: string;
  /** 考生作答可读文本 */
  answerText: string;
  /** 标准答案原始内容 */
  standardAnswer: string;
  /** 标准答案可读文本 */
  standardAnswerText: string;
  /** 解析富文本 */
  analysis: string;
  /** 当前得分（未阅为 null） */
  score?: number;
  /** 是否正确 1正确 / 2错误 / 0未判 */
  correct?: number;
  /** 阅卷状态 pending / marked */
  status: string;
  /** 阅卷方式 manual人工 / ai智能 */
  markType: string;
  /** 阅卷评语 */
  markComment: string;
  /** AI 建议分 */
  aiScore?: number;
  /** AI 建议理由 */
  aiReason: string;
  /** AI 状态 none / pending / done / failed */
  aiStatus: string;
}

/** 阅卷操作日志 */
export interface MarkLogVO {
  logId: string;
  /** 阅卷任务ID */
  taskId: string;
  /** 阅卷明细ID */
  itemId: string;
  /** 试题ID */
  questionId: string;
  /** 动作 create建任务 / score打分 / rescore改分 / ai预评 / finish完成 */
  action: string;
  /** 原分数 */
  oldScore?: number;
  /** 新分数 */
  newScore?: number;
  /** 阅卷方式 */
  markType: string;
  /** 备注 */
  remark: string;
  /** 操作人 */
  operatorName: string;
  /** 操作时间 */
  createTime: string;
}

/** 单题打分入参 */
export interface MarkScoreForm {
  /** 阅卷明细ID */
  itemId: string | number;
  /** 得分 */
  score: number;
  /** 是否正确 1正确 / 2错误 */
  correct?: number;
  /** 评语 */
  markComment?: string;
  /** 是否采用 AI 建议分 */
  useAiScore?: boolean;
}
