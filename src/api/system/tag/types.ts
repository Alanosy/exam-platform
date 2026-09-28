export interface TagVO {
  /**
   * 主键ID
   */
  id: string | number;

  /**
   * 标签名称
   */
  tagName: string;

  /**
   * 创建人ID
   */
  creatorId: string | number;

}

export interface TagForm extends BaseEntity {
  /**
   * 主键ID
   */
  id?: string | number;

  /**
   * 标签名称
   */
  tagName?: string;

  /**
   * 创建人ID
   */
  creatorId?: string | number;

}

export interface TagQuery extends PageQuery {

  /**
   * 标签名称
   */
  tagName?: string;

  /**
   * 创建人ID
   */
  creatorId?: string | number;

  /**
   * 日期范围参数
   */
  params?: any;
}



