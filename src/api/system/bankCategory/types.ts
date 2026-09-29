export interface BankCategoryVO {
  /**
   * 分类id
   */
  id: string | number;

  /**
   * 父分类id，0根节点
   */
  parentId: string | number;

  /**
   * 分类名称
   */
  categoryName: string;

  /**
   * 排序
   */
  sort: number;

  /**
   * 
   */
  isDeleted: number;

  /**
   * 创建时间
   */
  createTime?: string;

}

/**
 * 树形分类（父节点带 children）
 */
export interface BankCategoryTreeVO extends BankCategoryVO {
  children?: BankCategoryTreeVO[];
}

export interface BankCategoryForm extends BaseEntity {
  /**
   * 分类id
   */
  id?: string | number;

  /**
   * 父分类id，0根节点
   */
  parentId?: string | number;

  /**
   * 分类名称
   */
  categoryName?: string;

  /**
   * 排序
   */
  sort?: number;

  /**
   * 
   */
  isDeleted?: number;

}

export interface BankCategoryQuery extends PageQuery {

  /**
   * 父分类id，0根节点
   */
  parentId?: string | number;

  /**
   * 分类名称
   */
  categoryName?: string;

  /**
   * 排序
   */
  sort?: number;

  /**
   * 
   */
  isDeleted?: number;

  /**
   * 日期范围参数
   */
  params?: any;
}



