package org.dromara.exam.stat.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 考试分数段分布 stat_exam_score_segment
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stat_exam_score_segment")
public class StatExamScoreSegment extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long examId;

    private String segmentLabel;

    private BigDecimal segmentMin;

    private BigDecimal segmentMax;

    private Integer personCount;

    private Integer sort;
}
