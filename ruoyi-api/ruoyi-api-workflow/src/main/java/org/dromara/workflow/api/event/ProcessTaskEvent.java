package org.dromara.workflow.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.utils.SpringUtils;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;

import java.io.Serial;
import java.util.Map;

/**
 * 流程任务监听
 *
 * @author may
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessTaskEvent extends RemoteApplicationEvent {

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
     * 节点类型（0开始节点 1中间节点 2结束节点 3互斥网关 4并行网关）
     */
    private Integer nodeType;

    /**
     * 流程节点编码
     */
    private String nodeCode;

    /**
     * 流程节点名称
     */
    private String nodeName;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 实例id
     */
    private Long instanceId;

    /**
     * 业务id
     */
    private String businessId;

    /**
     * 流程状态
     */
    private String status;

    /**
     * 办理参数
     */
    private Map<String, Object> params;

    public ProcessTaskEvent() {
        super(new Object(), SpringUtils.getApplicationName(), DEFAULT_DESTINATION_FACTORY.getDestination(null));
    }
}
