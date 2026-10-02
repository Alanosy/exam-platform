export interface ExamVO {
  /** 主键 */
  id: string | number;

  /** 考试名称 */
  examName: string;

  /** 考试描述 */
  examDesc: string;

  /** 关联试卷ID */
  paperId: string | number;

  /** 考试类型 1正式考试 / 2练习考试 */
  examType: string;

  /** 开始时间 */
  startTime: string;

  /** 结束时间 */
  endTime: string;

  /** 考试限时（分钟），0 表示不限时 */
  duration: number;

  /** 是否允许迟到入场 0否 1是 */
  allowLate: number;

  /** 允许迟到分钟数，超过则无法进入 */
  lateMinute: number;

  /** 是否允许重考 0否 1是 */
  allowRetry: number;

  /** 单个考生最大重考次数 */
  maxRetryCount: number;

  /** 答案展示时机 none不展示 / after_submit交卷后 / after_exam考试结束后 */
  showAnswerMode: string;

  /**
   * 客观题部分得分开关 '0'必须全对 / '1'启用部分得分
   *
   * 作用于多选题（漏选）与填空题（只答对部分空）。
   * 多选题选中任何错误选项一律 0 分，防止「全选蒙满分」。
   */
  partialScore: string;

  /** 部分正确时的得分比例（%），100按命中比例 / 50一律半数 */
  partialScoreRate: number;

  /** 防作弊配置（JSON） */
  antiCheatConfig: string;

  /** 及格证书模板ID，为空表示本场考试不发证书 */
  certId?: string | number;

  /** 参加方式 white白名单 / public公开链接 */
  participantType: string;

  /** 公开考试的加入码，用于拼加入链接，为空表示非公开考试 */
  joinCode: string;

  /** 公开考试的参与密码，为空表示无密码 */
  joinPassword: string;

  /** 公开链接有效期，为空表示与考试结束时间一致 */
  joinExpireTime: string;

  /** 状态 not_start未开始 / ongoing进行中 / finished已结束 / archived已归档 */
  status: string;

  /** 创建人ID，新增时由后端自动填充 */
  creatorId: string | number;

  /** 创建人名称，由后端按 creatorId 翻译，仅用于展示 */
  creatorName: string;

  /** 白名单人数，非白名单考试为 0，后端查询时统计回填 */
  whiteUserCount?: number;
}

export interface ExamForm extends BaseEntity {
  id?: string | number;
  examName?: string;
  examDesc?: string;
  paperId?: string | number;
  /** 考试类型 1正式考试 / 2练习考试 */
  examType?: string;
  startTime?: string;
  endTime?: string;
  duration?: number;
  allowLate?: number;
  lateMinute?: number;
  allowRetry?: number;
  maxRetryCount?: number;
  showAnswerMode?: string;

  /** 客观题部分得分开关 '0'必须全对 / '1'启用部分得分 */
  partialScore?: string;

  /** 部分正确时的得分比例（%） */
  partialScoreRate?: number;
  antiCheatConfig?: string;
  participantType?: string;
  joinCode?: string;
  joinPassword?: string;
  joinExpireTime?: string;
  status?: string;
  creatorId?: string | number;
  /** 及格证书模板ID，留空表示不发证书 */
  certId?: string | number;
}

/** 防作弊配置，序列化成 JSON 存 exam.anti_cheat_config */
export interface AntiCheatConfig {
  /** 允许切屏次数，0 表示不限制 */
  switchScreen: number;
  /** 禁止复制粘贴 0否 1是 */
  copyPaste: number;
  /** 摄像头抓拍 0否 1是 */
  camera: number;
  /** 强制全屏 0否 1是 */
  fullScreen: number;
  /** 摄像头抓拍间隔（秒），最小 15 秒 */
  cameraInterval: number;
  /** 允许粘贴次数，0 表示不限制，超过则强制交卷 */
  maxPaste: number;
  /** 允许退出全屏次数，0 表示不限制，超过则强制交卷 */
  maxExitFullscreen: number;
  /** 开发者工具检测 0关 1开（只记录告警） */
  devtool: number;
  /** 多标签页/多端检测 0关 1开（只记录告警） */
  multitab: number;
}

/** 考生通过加入链接看到的考试概要（不含参与密码本身） */
export interface ExamJoinVO {
  /** 考试ID */
  examId: string | number;

  /** 考试名称 */
  examName: string;

  /** 考试描述 */
  examDesc: string;

  /** 开始时间 */
  startTime: string;

  /** 结束时间 */
  endTime: string;

  /** 考试限时（分钟），0 表示不限时 */
  duration: number;

  /** 状态 not_start未开始 / ongoing进行中 / finished已结束 / archived已归档 */
  status: string;

  /** 加入链接有效期，为空表示与考试结束时间一致 */
  joinExpireTime: string;

  /** 是否需要输入参与密码 */
  needPassword: boolean;

  /** 当前登录用户是否已加入本场考试 */
  joined: boolean;

  /** 当前是否允许加入 */
  joinable: boolean;

  /** 不允许加入时的原因，允许加入时为空 */
  joinTip: string;
}

/** 白名单考生：exam_user 里只存 userId，昵称 / 部门名由后端远程补全后一起返回 */
export interface ExamWhiteUserVO {
  /** 考生用户ID */
  userId: string | number;

  /** 登录账号 */
  userName: string;

  /** 用户昵称 */
  nickName: string;

  /** 所属部门ID */
  deptId: string | number;

  /** 所属部门名称 */
  deptName: string;

  /** 手机号码 */
  phonenumber: string;
}

/**
 * 考试情况：一行 = 一个考生的一场作答
 */
export interface ExamSituationVO {
  /** 答卷记录ID */
  recordId: string;

  /** 考生用户ID */
  userId: string;

  /** 考生账号 */
  account: string;

  /** 考生姓名 */
  nickName: string;

  /** 所属部门 */
  deptName: string;

  /** 第几次参加，从1开始 */
  attemptNo: number;

  /** answering答题中 / submitted已交卷 / expired超时作废 */
  status: string;

  /** 开考时间 */
  startTime: string;

  /** 交卷时间，答题中为空 */
  submitTime: string;

  /** 用时（秒） */
  usedSeconds: number;

  /** 用时文案 mm:ss，导出用 */
  usedTimeLabel: string;

  /** 题目总数 */
  questionCount: number;

  /** 已作答题目数 */
  answeredCount: number;

  /** 客观题得分 */
  objectiveScore: number | string;

  /** 主观题得分 */
  subjectiveScore: number | string;

  /** 总分 */
  totalScore: number | string;

  /** 及格分 */
  passScore: number | string;

  /** 是否及格 1及格 0不及格；待阅时后端不下发 */
  passed?: number;

  /** 主观题没阅完为 true，此时总分还没定 */
  pendingMark: boolean;
}

/**
 * 考试情况：整场考试的概览
 */
export interface ExamSituationOverviewVO {
  /** 考试ID */
  examId: string;

  /** 考试名称 */
  examName: string;

  /** 考试类型 1正式考试 / 2练习考试 */
  examType: string;

  /** 考试状态 */
  status: string;

  /** 开始时间 */
  startTime: string;

  /** 结束时间 */
  endTime: string;

  /** 考试限时（分钟） */
  duration: number;

  /** 及格分 */
  passScore: number | string;

  /** 应考人数 */
  invitedCount: number;

  /** 参考人数（去重） */
  joinedCount: number;

  /** 已交卷份数 */
  submittedCount: number;

  /** 答题中份数 */
  answeringCount: number;

  /** 待阅份数，大于 0 才显示「去阅卷」 */
  pendingMarkCount: number;

  /** 平均分 */
  avgScore: number | string;

  /** 最高分 */
  maxScore: number | string;

  /** 最低分 */
  minScore: number | string;

  /** 及格人数 */
  passedCount: number;

  /** 及格率（%） */
  passRate: number | string;
}

export interface ExamSituationQuery extends PageQuery {
  /** 关键词：姓名 / 账号 / 部门 */
  keyword?: string;

  /** 答卷状态 answering / submitted / expired */
  status?: string;

  /** 是否及格 1及格 0不及格 */
  passed?: number;

  /** 只看待阅：1 */
  pendingMark?: number;
}

export interface ExamQuery extends PageQuery {
  /** 考试名称（模糊匹配） */
  examName?: string;

  /** 参加方式 */
  participantType?: string;

  /** 考试类型 1正式考试 / 2练习考试 */
  examType?: string;

  /** 状态 */
  status?: string;

  /** 日期范围参数 */
  params?: any;
}
