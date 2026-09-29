export interface PaperVO {
  /**
   * 试卷主键ID
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
   * 组卷模式 MANUAL手动选题 / RANDOM随机抽题(AI抽题)
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
   * 考试时长(分钟)，0代表不限时
   */
  timeLimit: number;

  /**
   * 可见性 private私有 / public公开
   */
  visibility: string;

  /**
   * 公开分享密码，公开模式生效，空则无密码
   */
  sharePassword: string;

  /**
   * 分享链接过期时间，NULL永久有效
   */
  shareExpireTime: string;

  /**
   * 随机抽题规则，paper_type=RANDOM时生效：{bankId,questionType,difficulty,count,scorePerQuestion}
   */
  randomRule: string;

  /**
   * draft草稿 / ready已组卷 / archived归档
   */
  status: string;

  /**
   * 创建人ID
   */
  creatorId: string | number;

}

export interface PaperForm extends BaseEntity {
  /**
   * 试卷主键ID
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
   * 组卷模式 MANUAL手动选题 / RANDOM随机抽题(AI抽题)
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
   * 考试时长(分钟)，0代表不限时
   */
  timeLimit?: number;

  /**
   * 可见性 private私有 / public公开
   */
  visibility?: string;

  /**
   * 公开分享密码，公开模式生效，空则无密码
   */
  sharePassword?: string;

  /**
   * 分享链接过期时间，NULL永久有效
   */
  shareExpireTime?: string;

  /**
   * 随机抽题规则，paper_type=RANDOM时生效：{bankId,questionType,difficulty,count,scorePerQuestion}
   */
  randomRule?: string;

  /**
   * draft草稿 / ready已组卷 / archived归档
   */
  status?: string;

  /**
   * 创建人ID
   */
  creatorId?: string | number;

}

export interface PaperQuery extends PageQuery {

  /**
   * 试卷名称
   */
  paperName?: string;

  /**
   * 试卷描述
   */
  paperDesc?: string;

  /**
   * 组卷模式 MANUAL手动选题 / RANDOM随机抽题(AI抽题)
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
   * 考试时长(分钟)，0代表不限时
   */
  timeLimit?: number;

  /**
   * 可见性 private私有 / public公开
   */
  visibility?: string;

  /**
   * 公开分享密码，公开模式生效，空则无密码
   */
  sharePassword?: string;

  /**
   * 分享链接过期时间，NULL永久有效
   */
  shareExpireTime?: string;

  /**
   * 随机抽题规则，paper_type=RANDOM时生效：{bankId,questionType,difficulty,count,scorePerQuestion}
   */
  randomRule?: string;

  /**
   * draft草稿 / ready已组卷 / archived归档
   */
  status?: string;

  /**
   * 创建人ID
   */
  creatorId?: string | number;

  /**
   * 日期范围参数
   */
  params?: any;
}



