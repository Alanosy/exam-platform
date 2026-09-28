export interface TagRelVO {
  /**
   * 主键ID
   */
  id: string | number;

  /**
   * 试题ID
   */
  questionId: string | number;

  /**
   * 标签ID
   */
  tagId: string | number;

}

export interface TagRelForm extends BaseEntity {
  /**
   * 主键ID
   */
  id?: string | number;

  /**
   * 试题ID
   */
  questionId?: string | number;

  /**
   * 标签ID
   */
  tagId?: string | number;

}

export interface TagRelQuery extends PageQuery {

  /**
   * 试题ID
   */
  questionId?: string | number;

  /**
   * 标签ID
   */
  tagId?: string | number;

  /**
   * 日期范围参数
   */
  params?: any;
}



