package org.dromara.workflow.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.utils.SpringUtils;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;

import java.io.Serial;

/**
 * 流程创建任务监听
 *
 * @author may
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessCreateTaskEvent extends RemoteApplicationEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 流程定义编码
     */
    private String flowCode;

    /**
     * 审批节点编码
     */
    private String nodeCode;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 业务id
     */
    private String businessId;

    public ProcessCreateTaskEvent() {
        super(new Object(), SpringUtils.getApplicationName(), DEFAULT_DESTINATION_FACTORY.getDestination(null));
    }
}
