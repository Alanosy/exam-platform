package org.dromara.workflow.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.warm.flow.core.dto.FlowParams;
import org.dromara.warm.flow.core.handler.PermissionHandler;
import org.dromara.warm.flow.core.service.impl.TaskServiceImpl;
import org.dromara.workflow.common.ConditionalOnEnable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 办理人权限处理器
 *
 * @author AprilWind
 */
@ConditionalOnEnable
@RequiredArgsConstructor
@Component
@Slf4j
public class WorkflowPermissionHandler implements PermissionHandler {

    /**
     * 审批前获取当前办理人，办理时会校验的该权限集合
     * 后续在{@link TaskServiceImpl#checkAuth(Task, FlowParams)} 中调用
     * 返回当前用户权限集合
     */
    @Override
    public List<String> permissions() {
        return Collections.singletonList(LoginHelper.getUserIdStr());
    }

    /**
     * 获取当前办理人
     *
     * @return 当前办理人
     */
    @Override
    public String getHandler() {
        return LoginHelper.getUserIdStr();
    }

}
