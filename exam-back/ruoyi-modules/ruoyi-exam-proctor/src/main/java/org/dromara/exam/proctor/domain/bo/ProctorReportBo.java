package org.dromara.exam.proctor.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 事件批量上报参数
 *
 * @author ruoyi
 */
@Data
public class ProctorReportBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 监考会话ID */
    @NotNull(message = "监考会话ID不能为空")
    private Long sessionId;

    /** 本次要上报的事件，最多 50 条 */
    @Valid
    private List<ProctorEventBo> events;
}
