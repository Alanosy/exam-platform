/** 防作弊规则（由 exam.anti_cheat_config 解析后在开考时下发） */
export interface ProctorRule {
  /** 允许切屏次数，0 不限制，超过强制交卷 */
  switchScreen: number;
  /** 禁止复制粘贴 0否 1是 */
  copyPaste: number;
  /** 摄像头抓拍 0否 1是 */
  camera: number;
  /** 强制全屏 0否 1是 */
  fullScreen: number;
  /** 摄像头抓拍间隔（秒） */
  cameraInterval: number;
  /** 允许粘贴次数，0 不限制 */
  maxPaste: number;
  /** 允许退出全屏次数，0 不限制 */
  maxExitFullscreen: number;
  /** 开发者工具检测 0关 1开 */
  devtool: number;
  /** 多标签页检测 0关 1开 */
  multitab: number;
}

/** 监考会话 */
export interface ProctorSessionVO {
  id: string;
  examId: string;
  examName: string;
  recordId: string;
  userId: string;
  account: string;
  nickName: string;
  attemptNo: number;
  switchCount: number;
  blurCount: number;
  copyCount: number;
  pasteCount: number;
  cutCount: number;
  contextmenuCount: number;
  exitFullscreenCount: number;
  cameraCount: number;
  devtoolCount: number;
  multitabCount: number;
  maxSwitch: number;
  maxExitFullscreen: number;
  maxPaste: number;
  cameraInterval: number;
  /** 是否已强制交卷：后端 Long 会被序列化成字符串（"0" / "1"） */
  forceSubmit: string;
  /** online / offline / submitted / force_submit */
  status: string;
  /** normal / suspect / serious */
  riskLevel: string;
  riskScore: number;
  startTime: string;
  lastActiveTime: string;
  endTime: string;
  durationSeconds: number;
  ip: string;
  device: string;
  /** 只在开启会话时返回 */
  rule?: ProctorRule;
}

/** 考生端上报的一条事件 */
export interface ProctorEventBO {
  eventType: string;
  content?: string;
  extra?: string;
  /** yyyy-MM-dd HH:mm:ss */
  eventTime?: string;
}

/** 上报结果 */
export interface ProctorReportVO {
  sessionId: string;
  switchCount: number;
  pasteCount: number;
  exitFullscreenCount: number;
  cameraCount: number;
  maxSwitch: number;
  maxPaste: number;
  maxExitFullscreen: number;
  riskLevel: string;
  /** 是否已达到强制交卷条件 */
  exceed: boolean;
  exceedReason: string;
  savedCount: number;
}

/** 事件流水 */
export interface ProctorEventVO {
  id: string;
  sessionId: string;
  examId: string;
  recordId: string;
  account: string;
  nickName: string;
  eventType: string;
  eventName: string;
  /** info / warn / danger */
  level: string;
  content: string;
  extra: string;
  eventTime: string;
  createTime: string;
}

/** 摄像头抓拍 */
export interface ProctorSnapshotVO {
  id: string;
  sessionId: string;
  account: string;
  nickName: string;
  url: string;
  eventType: string;
  captureTime: string;
}

/** 监考概览 */
export interface ProctorOverviewVO {
  examId: string;
  examName: string;
  totalCount: number;
  onlineCount: number;
  offlineCount: number;
  submittedCount: number;
  suspectCount: number;
  seriousCount: number;
  switchTotal: number;
  pasteTotal: number;
  cameraTotal: number;
  forceSubmitCount: number;
}

/** 一场考试的监考汇总（监考中心第一层，先挑出有问题的那场考试） */
export interface ProctorExamGroupVO {
  examId: string;
  examName: string;
  /** 参加人数 */
  totalCount: number;
  onlineCount: number;
  offlineCount: number;
  submittedCount: number;
  suspectCount: number;
  seriousCount: number;
  forceSubmitCount: number;
  switchTotal: number;
  pasteTotal: number;
  cameraTotal: number;
  /** 本场最高风险分 */
  maxRiskScore: number;
  /** 本场最高风险等级 normal / suspect / serious */
  riskLevel: string;
  lastActiveTime: string;
}

/** 考试分组查询条件 */
export interface ProctorExamGroupQuery extends Record<string, unknown> {
  keyword?: string;
  onlyRisk?: boolean;
}

/** 监考会话查询条件 */
export interface ProctorSessionQuery extends Record<string, unknown> {
  examId?: string;
  keyword?: string;
  status?: string;
  riskLevel?: string;
  onlyRisk?: boolean;
}

/** 事件流水查询条件 */
export interface ProctorEventQuery extends Record<string, unknown> {
  sessionId?: string;
  examId?: string;
  eventType?: string;
  level?: string;
  onlyWarn?: boolean;
}
