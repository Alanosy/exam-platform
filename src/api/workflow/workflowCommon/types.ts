import { NodeConfigVO } from '@/api/workflow/nodeConfig/types';
import { DefinitionConfigVO } from '@/api/workflow/definitionConfig/types';

export interface RouterJumpVo {
  wfNodeConfigVo: NodeConfigVO;
  wfDefinitionConfigVo: DefinitionConfigVO;
  businessKey: string;
  taskId: string;
  type: string;
}

export interface StartProcessBo {
  businessKey: string | number;
  tableName: string;
  variables: any;
}
