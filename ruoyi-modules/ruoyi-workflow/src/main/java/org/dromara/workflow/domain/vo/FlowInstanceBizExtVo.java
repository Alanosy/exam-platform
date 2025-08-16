package org.dromara.workflow.domain.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.workflow.domain.FlowInstanceBizExt;

import java.io.Serial;
import java.io.Serializable;


/**
 * 流程实例业务扩展视图对象 flow_instance_biz_ext
 *
 * @author may
 * @date 2025-08-05
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = FlowInstanceBizExt.class)
public class FlowInstanceBizExtVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 流程实例ID
     */
    @ExcelProperty(value = "流程实例ID")
    private Long instanceId;

    /**
     * 业务ID
     */
    @ExcelProperty(value = "业务ID")
    private String businessId;

    /**
     * 业务编码
     */
    @ExcelProperty(value = "业务编码")
    private String businessCode;

    /**
     * 业务标题
     */
    @ExcelProperty(value = "业务标题")
    private String businessTitle;


}
