package org.dromara.workflow.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.workflow.domain.FlowInstanceBizExt;

/**
 * 流程实例业务扩展业务对象 flow_instance_biz_ext
 *
 * @author may
 * @date 2025-08-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = FlowInstanceBizExt.class, reverseConvertGenerate = false)
public class FlowInstanceBizExtBo extends BaseEntity {

    /**
     * 主键
     */
    private Long id;

    /**
     * 流程实例ID
     */
    private Long instanceId;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 业务编码
     */
    private String businessCode;

    /**
     * 业务标题
     */
    private String businessTitle;


}
