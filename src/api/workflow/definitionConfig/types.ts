import { FormManageVO } from '@/api/workflow/formManage/types';

export interface DefinitionConfigVO {
  /**
   * 主键
   */
  id: string | number;

  /**
   * 表名
   */
  tableName?: string;

  /**
   * 流程定义ID
   */
  definitionId: string | number;

  /**
   * 流程KEY
   */
  processKey: string;

  /**
   * 流程版本
   */
  version?: string | number;

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
   * 表名
   */
  tableName?: string;

  /**
   * 流程定义ID
   */
  definitionId?: string | number;

  /**
   * 流程KEY
   */
  processKey?: string;

  /**
   * 流程版本
   */
  version?: string | number;

  /**
   * 备注
   */
  remark?: string;

  /**
   * 表单管理
   */
  wfFormManageVo?: FormManageVO;
}

export interface DefinitionConfigQuery extends PageQuery {
  /**
   * 表名
   */
  tableName?: string;

  /**
   * 流程定义ID
   */
  definitionId?: string | number;

  /**
   * 流程KEY
   */
  processKey?: string;

  /**
   * 流程版本
   */
  version?: string | number;

  /**
   * 表单管理
   */
  wfFormManageVo: FormManageVO;
}
