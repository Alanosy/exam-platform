/**
 * 试题选项（随试题一起嵌套提交）
 *
 * 注意：是否为正确答案不落 option 表，统一由 question.answer 的 JSON 描述，
 * 详见 src/views/system/question/questionMeta.ts
 */
export interface QuestionOption {
  /**
   * 主键ID，编辑已存在的选项时携带
   */
  id?: string | number;

  /**
   * 试题ID，新增时由后端回填，无需前端传递
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
   * 是否为正确答案
   *
   * 前端编辑态使用。若后端 option 表已冗余该字段则随请求提交，
   * 否则请忽略（真正的答案以 question.answer 为准）
   */
  isRight?: boolean;
}

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

  /**
   * 选项列表，详情接口返回时携带
   */
  options?: QuestionOption[];
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

  /**
   * 选项列表，新增/修改时随试题一并提交
   */
  options?: QuestionOption[];
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
