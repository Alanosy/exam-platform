import { FormManageVO } from '@/api/workflow/formManage/types';

export interface DefinitionConfigVO {
  /**
   * 主键
   */
  id: string | number;

  /**
   * 表单ID
   */
  formId?: string | number;

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

  /**
   * 表单管理
   */
  wfFormManageVo: FormManageVO;

}

export interface DefinitionConfigForm extends BaseEntity {
  /**
   * 主键
   */
  id?: string | number;

  /**
   * 表单ID
   */
  formId?: string | number;

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

  /**
   * 表单管理
   */
  wfFormManageVo: FormManageVO;


}

export interface DefinitionConfigQuery extends PageQuery {

  /**
   * 表单ID
   */
  formId?: string | number;

  /**
   * 流程定义ID
   */
  definitionId?: string | number;

  /**
   * 流程KEY
   */
  processKey?: string;

  /**
   * 表单管理
   */
  wfFormManageVo: FormManageVO;

}



