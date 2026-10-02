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
