import { NodeConfigVO } from '@/api/workflow/nodeConfig/types';

export interface RouterJumpVo {
    wfNodeConfigVo: NodeConfigVO;
    businessKey: string;
    taskId: string;
    type: string;
}

export interface StartProcessBo {
    businessKey:  string | number;
    tableName: string;
    variables: any;
}