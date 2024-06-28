import { FormManageVO } from '@/api/workflow/formManage/types';

export interface NodeConfigVO {
  /**
   * 主键
   */
  id: string | number;

  /**
   * 表单id
   */
  formId: string | number;

  /**
   * 表单类型
   */
  formType: string;

  /**
   * 节点名称
   */
  nodeName: string;

  /**
   * 节点id
   */
  nodeId: string | number;

  /**
   * 流程定义id
   */
  definitionId: string | number;

  /**
   * 表单管理
   */
  wfFormManageVo: FormManageVO;
}
