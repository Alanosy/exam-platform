package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 知识点覆盖统计
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteAiKnowledgeCoverageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 知识点 */
    private String point;

    /** 考查次数 */
    private Integer count;

    /** 占比 0-1 */
    private BigDecimal ratio;
}
