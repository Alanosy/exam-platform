import { RouterJumpVo } from '@/api/workflow/workflowCommon/types';

export default {
    routerJump(routerJumpVo: RouterJumpVo,proxy){
        if (routerJumpVo.wfNodeConfigVo && routerJumpVo.wfNodeConfigVo.formType === 'static' && routerJumpVo.wfNodeConfigVo.wfFormManageVo) {
            proxy.$tab.closePage(proxy.$route);
            proxy.$router.push({
                path: `${routerJumpVo.wfNodeConfigVo.wfFormManageVo.router}`,
                query: {
                    id: routerJumpVo.businessKey,
                    type: routerJumpVo.type,
                    taskId: routerJumpVo.taskId
                }
            });
        } else if (routerJumpVo.wfNodeConfigVo && routerJumpVo.wfNodeConfigVo.formType === 'dynamic' && routerJumpVo.wfNodeConfigVo.wfFormManageVo) {
            proxy.$tab.closePage(proxy.$route);
            proxy.$router.push({
                path: `${routerJumpVo.wfNodeConfigVo.wfFormManageVo.router}`,
                query: {
                    id: routerJumpVo.businessKey,
                    type: routerJumpVo.type,
                    taskId: routerJumpVo.taskId
                }
            });
        }else if (routerJumpVo.wfDefinitionConfigVo && routerJumpVo.wfDefinitionConfigVo.wfFormManageVo && routerJumpVo.wfDefinitionConfigVo.wfFormManageVo.formType === 'static') {
            proxy.$tab.closePage(proxy.$route);
            proxy.$router.push({
                path: `${routerJumpVo.wfDefinitionConfigVo.wfFormManageVo.router}`,
                query: {
                    id: routerJumpVo.businessKey,
                    type: routerJumpVo.type,
                    taskId: routerJumpVo.taskId
                }
            });
        }else if (routerJumpVo.wfDefinitionConfigVo && routerJumpVo.wfDefinitionConfigVo.wfFormManageVo && routerJumpVo.wfDefinitionConfigVo.wfFormManageVo.formType === 'dynamic') {
            proxy.$tab.closePage(proxy.$route);
            proxy.$router.push({
                path: `${routerJumpVo.wfDefinitionConfigVo.wfFormManageVo.router}`,
                query: {
                    id: routerJumpVo.businessKey,
                    type: routerJumpVo.type,
                    taskId: routerJumpVo.taskId
                }
            });
        } else {
            proxy?.$modal.msgError('请到流程定义菜单配置路由！');
        }
    }
}