export interface QuestionVO {
  /**
   * 主键ID
   */
  id: string | number;

  /**
   * 所属题库ID
   */
  bankId: string | number;

  /**
   * 题干富文本
   */
  title: string;

  /**
   * 题型 SINGLE单选,MULTIPLE多选,JUDGE判断,BLANK填空,SHORT_ANSWER简答,ESSAY论述,CODE代码题,UPLOAD_FILE文件上传,MATCH匹配题
   */
  questionType: string;

  /**
   * 难度 easy简单 medium中等 hard困难
   */
  difficulty: string;

  /**
   * 题目默认分值
   */
  score: number;

  /**
   * 试题解析富文本
   */
  analysis: string;

  /**
   * 参考答案JSON，不同题型结构不同
   */
  answer: string;

  /**
   * 题目创建人ID
   */
  createUser: number;

  /**
   * 0草稿 1启用 2废弃
   */
  status: number;

}

export interface QuestionForm extends BaseEntity {
  /**
   * 主键ID
   */
  id?: string | number;

  /**
   * 所属题库ID
   */
  bankId?: string | number;

  /**
   * 题干富文本
   */
  title?: string;

  /**
   * 题型 SINGLE单选,MULTIPLE多选,JUDGE判断,BLANK填空,SHORT_ANSWER简答,ESSAY论述,CODE代码题,UPLOAD_FILE文件上传,MATCH匹配题
   */
  questionType?: string;

  /**
   * 难度 easy简单 medium中等 hard困难
   */
  difficulty?: string;

  /**
   * 题目默认分值
   */
  score?: number;

  /**
   * 试题解析富文本
   */
  analysis?: string;

  /**
   * 参考答案JSON，不同题型结构不同
   */
  answer?: string;

  /**
   * 题目创建人ID
   */
  createUser?: number;

  /**
   * 0草稿 1启用 2废弃
   */
  status?: number;

}

export interface QuestionQuery extends PageQuery {

  /**
   * 所属题库ID
   */
  bankId?: string | number;

  /**
   * 题干富文本
   */
  title?: string;

  /**
   * 题型 SINGLE单选,MULTIPLE多选,JUDGE判断,BLANK填空,SHORT_ANSWER简答,ESSAY论述,CODE代码题,UPLOAD_FILE文件上传,MATCH匹配题
   */
  questionType?: string;

  /**
   * 难度 easy简单 medium中等 hard困难
   */
  difficulty?: string;

  /**
   * 题目默认分值
   */
  score?: number;

  /**
   * 试题解析富文本
   */
  analysis?: string;

  /**
   * 参考答案JSON，不同题型结构不同
   */
  answer?: string;

  /**
   * 题目创建人ID
   */
  createUser?: number;

  /**
   * 0草稿 1启用 2废弃
   */
  status?: number;

  /**
   * 日期范围参数
   */
  params?: any;
}



