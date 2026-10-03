package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * AI 评分命中的要点明细
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteAiMatchPointVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 要点描述 */
    private String point;

    /** 是否命中 */
    private Boolean got;

    /** 该要点得分 */
    private BigDecimal score;
}
