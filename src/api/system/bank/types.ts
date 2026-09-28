export interface BankVO {
  /**
   * 主键ID
   */
  id: string | number;

  /**
   * 题库名称
   */
  bankName: string;

  /**
   * 题库描述
   */
  bankDesc: string;

  /**
   * 创建人用户ID
   */
  creatorId: string | number;

  /**
   * 可见性 private私有 / public公开
   */
  visibility: string;

  /**
   * 状态 0草稿 1正常 2归档
   */
  status: number;

}

export interface BankForm extends BaseEntity {
  /**
   * 主键ID
   */
  id?: string | number;

  /**
   * 题库名称
   */
  bankName?: string;

  /**
   * 题库描述
   */
  bankDesc?: string;

  /**
   * 创建人用户ID
   */
  creatorId?: string | number;

  /**
   * 可见性 private私有 / public公开
   */
  visibility?: string;

  /**
   * 状态 0草稿 1正常 2归档
   */
  status?: number;

}

export interface BankQuery extends PageQuery {

  /**
   * 题库名称
   */
  bankName?: string;

  /**
   * 题库描述
   */
  bankDesc?: string;

  /**
   * 创建人用户ID
   */
  creatorId?: string | number;

  /**
   * 可见性 private私有 / public公开
   */
  visibility?: string;

  /**
   * 状态 0草稿 1正常 2归档
   */
  status?: number;

  /**
   * 日期范围参数
   */
  params?: any;
}



