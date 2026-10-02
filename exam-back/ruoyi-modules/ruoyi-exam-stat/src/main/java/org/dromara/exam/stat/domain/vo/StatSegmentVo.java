package org.dromara.exam.stat.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.exam.stat.domain.StatExamScoreSegment;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分数段分布
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@AutoMapper(target = StatExamScoreSegment.class)
public class StatSegmentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long examId;

    private String segmentLabel;

    private BigDecimal segmentMin;

    private BigDecimal segmentMax;

    private Integer personCount;

    private Integer sort;

    /** 占比% */
    private BigDecimal rate;

    /** 累计占比% */
    private BigDecimal cumulativeRate;
}
