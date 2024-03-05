export interface CategoryVO {
  /**
   * 主键
   */
  id: string;

  /**
   * 分类名称
   */
  categoryName: string;

  /**
   * 分类编码
   */
  categoryCode: string;

  /**
   * 父级id
   */
  parentId: string | number;

  /**
   * 排序
   */
  sortNum: number;

  children?: CategoryVO[];
}

export interface CategoryForm extends BaseEntity {
  /**
   * 主键
   */
  id?: string | number;

  /**
   * 分类名称
   */
  categoryName?: string;

  /**
   * 分类编码
   */
  categoryCode?: string;

  /**
   * 父级id
   */
  parentId?: string | number;

  /**
   * 排序
   */
  sortNum?: number;
}

export interface CategoryQuery extends PageQuery {
  /**
   * 分类名称
   */
  categoryName?: string;

  /**
   * 分类编码
   */
  categoryCode?: string;
}
