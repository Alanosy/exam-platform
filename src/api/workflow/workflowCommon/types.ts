import { NodeConfigVO } from '@/api/workflow/nodeConfig/types';

export interface RouterJumpVo {
    wfNodeConfigVo: NodeConfigVO;
    businessKey: string;
    taskId: string;
    type: string;
}