export interface OptionVO {
  /**
   * 主键ID
   */
  id: string | number;

  /**
   * 试题ID
   */
  questionId: string | number;

  /**
   * 选项标识 A/B/C/D
   */
  optionKey: string;

  /**
   * 选项内容富文本
   */
  optionContent: string;

  /**
   * 排序号
   */
  sort: number;

}

export interface OptionForm extends BaseEntity {
  /**
   * 主键ID
   */
  id?: string | number;

  /**
   * 试题ID
   */
  questionId?: string | number;

  /**
   * 选项标识 A/B/C/D
   */
  optionKey?: string;

  /**
   * 选项内容富文本
   */
  optionContent?: string;

  /**
   * 排序号
   */
  sort?: number;

}

export interface OptionQuery extends PageQuery {

  /**
   * 试题ID
   */
  questionId?: string | number;

  /**
   * 选项标识 A/B/C/D
   */
  optionKey?: string;

  /**
   * 选项内容富文本
   */
  optionContent?: string;

  /**
   * 排序号
   */
  sort?: number;

  /**
   * 日期范围参数
   */
  params?: any;
}



