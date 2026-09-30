/** 试卷-试题明细（组卷保存与回显使用） */
export interface PaperQuestionItem {
  /** 试题ID */
  questionId?: string | number;
  /** 该题目在本试卷内的分值，为空则取试卷默认单题分值 */
  paperScore?: number;
  /** 排序号，回显时由后端返回 */
  sort?: number;
  /** 明细主键，回显时由后端返回 */
  id?: string | number;
}

export interface PaperVO {
  /**
   * 试卷ID
   */
  id: string | number;

  /**
   * 试卷名称
   */
  paperName: string;

  /**
   * 试卷描述
   */
  paperDesc: string;

  /**
   * 组卷模式：MANUAL手动选题 / RANDOM随机抽题
   */
  paperType: string;

  /**
   * 试卷总分
   */
  totalScore: number;

  /**
   * 及格分数
   */
  passScore: number;

  /**
   * 考试时长(分钟)，0 表示不限时
   */
  timeLimit: number;

  /**
   * 可见性：private私有 / public公开
   */
  visibility: string;

  /**
   * 公开分享密码，公开模式生效，空则无密码
   */
  sharePassword: string;

  /**
   * 分享过期时间，空则永久有效
   */
  shareExpireTime: string;

  /**
   * 随机抽题规则(JSON)，组卷模式为 RANDOM 时生效
   */
  randomRule: string;

  /**
   * 状态：draft草稿 / ready已组卷 / archived归档
   */
  status: string;

  /**
   * 创建人ID，新增时由后端自动填充
   */
  creatorId: string | number;

  /**
   * 创建人名称，由后端按 creatorId 翻译，仅用于展示
   */
  creatorName: string;

  /**
   * 试卷分类，取字典 paper_category 的字典值
   */
  category: string;

  /**
   * 默认单题分值
   */
  defaultScore: number;

  /**
   * 是否开启题目乱序 0否 1是
   */
  questionShuffle: string;

  /**
   * 是否开启选项乱序 0否 1是
   */
  optionShuffle: string;

  /**
   * 客观题是否自动判分 0否 1是
   */
  autoJudge: string;

  /**
   * 主观题是否人工阅卷 0否 1是
   */
  manualReview: string;

  /**
   * 是否支持部分得分 0否 1是
   */
  partialScore: string;

  /**
   * 答错是否扣分 0否 1是
   */
  wrongDeduct: string;

  /**
   * 可见范围 SELF仅自己可编辑 / SHARED共享给其他管理员
   */
  shareScope: string;

  /**
   * 已选试题明细，详情接口返回
   */
  questions: PaperQuestionItem[];
}

export interface PaperForm extends BaseEntity {
  /**
   * 试卷ID
   */
  id?: string | number;

  /**
   * 试卷名称
   */
  paperName?: string;

  /**
   * 试卷描述
   */
  paperDesc?: string;

  /**
   * 组卷模式：MANUAL手动选题 / RANDOM随机抽题
   */
  paperType?: string;

  /**
   * 试卷总分
   */
  totalScore?: number;

  /**
   * 及格分数
   */
  passScore?: number;

  /**
   * 考试时长(分钟)，0 表示不限时
   */
  timeLimit?: number;

  /**
   * 可见性：private私有 / public公开
   */
  visibility?: string;

  /**
   * 公开分享密码，公开模式生效，空则无密码
   */
  sharePassword?: string;

  /**
   * 分享过期时间，空则永久有效
   */
  shareExpireTime?: string;

  /**
   * 随机抽题规则(JSON)，组卷模式为 RANDOM 时生效
   */
  randomRule?: string;

  /**
   * 状态：draft草稿 / ready已组卷 / archived归档
   */
  status?: string;

  /**
   * 创建人ID，不传时后端取当前登录用户填充
   */
  creatorId?: string | number;

  /**
   * 试卷分类，取字典 paper_category 的字典值
   */
  category?: string;

  /**
   * 默认单题分值
   */
  defaultScore?: number;

  /**
   * 是否开启题目乱序 0否 1是
   */
  questionShuffle?: string;

  /**
   * 是否开启选项乱序 0否 1是
   */
  optionShuffle?: string;

  /**
   * 客观题是否自动判分 0否 1是
   */
  autoJudge?: string;

  /**
   * 主观题是否人工阅卷 0否 1是
   */
  manualReview?: string;

  /**
   * 是否支持部分得分 0否 1是
   */
  partialScore?: string;

  /**
   * 答错是否扣分 0否 1是
   */
  wrongDeduct?: string;

  /**
   * 可见范围 SELF仅自己可编辑 / SHARED共享给其他管理员
   */
  shareScope?: string;

  /**
   * 本试卷的试题明细，组卷接口按数组顺序落 sort
   */
  questions?: PaperQuestionItem[];
}

export interface PaperQuery extends PageQuery {
  /**
   * 试卷名称
   */
  paperName?: string;

  /**
   * 组卷模式：MANUAL手动选题 / RANDOM随机抽题
   */
  paperType?: string;

  /**
   * 可见性：private私有 / public公开
   */
  visibility?: string;

  /**
   * 状态：draft草稿 / ready已组卷 / archived归档
   */
  status?: string;

  /**
   * 日期范围参数
   */
  params?: any;
}
