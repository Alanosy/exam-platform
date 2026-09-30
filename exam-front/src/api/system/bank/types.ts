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
   * 创建人昵称（后端由 creatorId 翻译，与试卷/考试模块一致）
   */
  creatorName: string;

  /**
   * 所属分类ID
   */
  categoryId: string | number;

  /**
   * 所属分类名称（后端由 categoryId 翻译）
   */
  categoryName: string;

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
   * 可见性（字典 bank_visibility_type）private私有 / public公开
   */
  visibility?: string;

  /**
   * 状态（字典 bank_status）0草稿 1正常 2归档，下拉框字典值为字符串
   */
  status?: string | number;

  /**
   * 所属分类ID
   */
  categoryId?: string | number;
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
  status?: string | number;

  /**
   * 所属分类ID（选中父分类时会带上所有子分类）
   */
  categoryId?: string | number;

  /**
   * 日期范围参数
   */
  params?: any;
}
