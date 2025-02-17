package org.dromara.workflow.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.utils.SpringUtils;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;

import java.io.Serial;
import java.util.Map;

/**
 * 总体流程监听
 *
 * @author may
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessEvent extends RemoteApplicationEvent {

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
     * 业务id
     */
    private String businessId;

    /**
     * 状态
     */
    private String status;

    /**
     * 办理参数
     */
    private Map<String, Object> params;

    /**
     * 当为true时为申请人节点办理
     */
    private boolean submit;

    public ProcessEvent() {
        super(new Object(), SpringUtils.getApplicationName(), DEFAULT_DESTINATION_FACTORY.getDestination(null));
    }

}
