export interface FormDefinitionVO {
  /**
   * 主键
   */
  id: string | number;

  /**
   * 路由地址
   */
  path: string;

  /**
   * 流程定义ID
   */
  definitionId: string | number;

  /**
   * 流程KEY
   */
  processKey: string;

  /**
   * 备注
   */
  remark: string;

}

export interface FormDefinitionForm extends BaseEntity {
  /**
   * 主键
   */
  id?: string | number;

  /**
   * 路由地址
   */
  path?: string;

  /**
   * 流程定义ID
   */
  definitionId?: string | number;

  /**
   * 流程KEY
   */
  processKey?: string;

  /**
   * 备注
   */
  remark?: string;

}

export interface FormDefinitionQuery extends PageQuery {

  /**
   * 路由地址
   */
  path?: string;

  /**
   * 流程定义ID
   */
  definitionId?: string | number;

  /**
   * 流程KEY
   */
  processKey?: string;

  /**
   * 日期范围参数
   */
  params?: any;
}



