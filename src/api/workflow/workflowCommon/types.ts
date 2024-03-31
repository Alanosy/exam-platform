import { NodeConfigVO } from '@/api/workflow/nodeConfig/types';
import { DefinitionConfigVO } from '@/api/workflow/definitionConfig/types';

export interface RouterJumpVo {
    wfDefinitionConfigVo: DefinitionConfigVO;
    wfNodeConfigVo: NodeConfigVO;
    businessKey: string;
    taskId: string;
    type: string;
}