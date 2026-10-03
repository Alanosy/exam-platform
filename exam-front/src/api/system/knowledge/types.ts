/**
 * 知识点相关类型
 *
 * 服务端：ruoyi-exam-question → KnowledgeController（/question/knowledge）
 * 结构：两级 —— parentId = 0 是「章节」，其下是「知识点」；全局共享、按租户隔离
 * ID 一律字符串透传（雪花 ID 19 位，超过 JS 安全整数范围）
 */

export interface KnowledgePointVO {
  /**
   * 主键ID
   */
  id: string | number;

  /**
   * 父级ID，0 表示章节（根节点）
   */
  parentId: string | number;

  /**
   * 知识点名称
   */
  name: string;

  /**
   * 排序
   */
  sort?: number;

  /**
   * 子级（仅树接口返回）
   */
  children?: KnowledgePointVO[];
}

export interface KnowledgePointForm {
  id?: string | number;
  parentId?: string | number;
  name?: string;
  sort?: number;
}

export interface KnowledgePointQuery extends PageQuery {
  name?: string;
  parentId?: string | number;
}

/**
 * 试题-知识点关联（含知识点名称）
 */
export interface QuestionKnowledgeVO {
  questionId: string | number;
  knowledgeId: string | number;
  knowledgeName?: string;
}
