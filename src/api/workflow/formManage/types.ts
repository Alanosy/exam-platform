export interface FormManageVO {
  /**
   * 主键
   */
  id: string | number;

  /**
   * 表单名称
   */
  formName: string;

  /**
   * 表单类型
   */
  formType: string;
  /**
   * 表单类型名称
   */
  formTypeName: string;

  /**
   * 路由地址/表单ID
   */
  router: string;

  /**
   * 备注
   */
  remark: string;
}

export interface FormManageForm extends BaseEntity {
  /**
   * 主键
   */
  id?: string | number;

  /**
   * 表单名称
   */
  formName?: string;

  /**
   * 表单类型
   */
  formType?: string;

  /**
   * 路由地址/表单ID
   */
  router?: string;

  /**
   * 备注
   */
  remark?: string;
}

export interface FormManageQuery extends PageQuery {
  /**
   * 表单名称
   */
  formName?: string;

  /**
   * 表单类型
   */
  formType?: string;
}
