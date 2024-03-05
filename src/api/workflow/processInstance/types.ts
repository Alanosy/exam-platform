import { TaskVO } from '@/api/workflow/task/types';

export interface ProcessInstanceQuery extends PageQuery {
  categoryCode?: string;
  name?: string;
  key?: string;
  startUserId?: string;
  businessKey?: string;
}

export interface ProcessInstanceVO extends BaseEntity {
  id: string;
  processDefinitionId: string;
  processDefinitionName: string;
  processDefinitionKey: string;
  processDefinitionVersion: string;
  deploymentId: string;
  businessKey: string;
  isSuspended?: any;
  tenantId: string;
  startTime: string;
  endTime?: string;
  startUserId: string;
  businessStatus: string;
  businessStatusName: string;
  taskVoList: TaskVO[];
}
