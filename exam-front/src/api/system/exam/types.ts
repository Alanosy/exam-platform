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

  /** 防作弊配置（JSON） */
  antiCheatConfig: string;

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
  antiCheatConfig?: string;
  participantType?: string;
  joinCode?: string;
  joinPassword?: string;
  joinExpireTime?: string;
  status?: string;
  creatorId?: string | number;
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
