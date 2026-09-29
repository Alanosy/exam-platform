export interface QuestionVO {
  /**
   * 主键ID
   */
  id: string | number;

  /**
   * 试卷ID
   */
  paperId: string | number;

  /**
   * 试题ID，关联question表
   */
  questionId: string | number;

  /**
   * 该题目在本试卷内分值，null使用question表默认score
   */
  paperScore: number;

  /**
   * 题目在试卷中的排序
   */
  sort: number;

}

export interface QuestionForm extends BaseEntity {
  /**
   * 主键ID
   */
  id?: string | number;

  /**
   * 试卷ID
   */
  paperId?: string | number;

  /**
   * 试题ID，关联question表
   */
  questionId?: string | number;

  /**
   * 该题目在本试卷内分值，null使用question表默认score
   */
  paperScore?: number;

  /**
   * 题目在试卷中的排序
   */
  sort?: number;

}

export interface QuestionQuery extends PageQuery {

  /**
   * 试卷ID
   */
  paperId?: string | number;

  /**
   * 试题ID，关联question表
   */
  questionId?: string | number;

  /**
   * 该题目在本试卷内分值，null使用question表默认score
   */
  paperScore?: number;

  /**
   * 题目在试卷中的排序
   */
  sort?: number;

  /**
   * 日期范围参数
   */
  params?: any;
}



