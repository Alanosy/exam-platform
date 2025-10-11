package org.dromara.workflow.api;

import lombok.extern.slf4j.Slf4j;
import org.dromara.workflow.api.domain.RemoteCompleteTask;
import org.dromara.workflow.api.domain.RemoteStartProcess;
import org.dromara.workflow.api.domain.RemoteStartProcessReturn;

import java.util.List;
import java.util.Map;

/**
 * 工作流服务(降级处理)
 *
 * @author Lion Li
 */
@Slf4j
public class RemoteWorkflowServiceMock implements RemoteWorkflowService {

    @Override
    public boolean deleteInstance(List<Long> businessIds) {
        log.warn("服务调用异常 -> 降级处理");
        return false;
    }

    @Override
    public String getBusinessStatusByTaskId(Long taskId) {
        log.warn("服务调用异常 -> 降级处理");
        return null;
    }

    @Override
    public String getBusinessStatus(String businessId) {
        log.warn("服务调用异常 -> 降级处理");
        return null;
    }

    @Override
    public void setVariable(Long instanceId, Map<String, Object> variable) {
        log.warn("服务调用异常 -> 降级处理");
    }

    @Override
    public Map<String, Object> instanceVariable(Long instanceId) {
        log.warn("服务调用异常 -> 降级处理");
        return null;
    }

    @Override
    public Long getInstanceIdByBusinessId(String businessId) {
        log.warn("服务调用异常 -> 降级处理");
        return null;
    }

    @Override
    public void syncDef(String tenantId) {
        log.warn("服务调用异常 -> 降级处理");
    }

    @Override
    public RemoteStartProcessReturn startWorkFlow(RemoteStartProcess startProcess) {
        log.warn("服务调用异常 -> 降级处理");
        return null;
    }

    @Override
    public boolean completeTask(RemoteCompleteTask completeTask) {
        log.warn("服务调用异常 -> 降级处理");
        return false;
    }

    @Override
    public boolean completeTask(Long taskId, String message) {
        log.warn("服务调用异常 -> 降级处理");
        return false;
    }

    @Override
    public boolean startCompleteTask(RemoteStartProcess startProcess) {
        log.warn("服务调用异常 -> 降级处理");
        return false;
    }

}
