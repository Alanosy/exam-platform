import { RouterJumpVo } from '@/api/workflow/workflowCommon/types';

export default {
  routerJump(routerJumpVo: RouterJumpVo, proxy) {
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
    } else {
      proxy?.$modal.msgError('请到模型配置菜单！');
    }
  }
};
