package org.dromara.exam.answer.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 单场考试的答卷统计
 *
 * <p>考试名 / 考试类型在考试库，这里只带回 examId，由统计服务回查后补名字，
 * 避免答题服务反过来依赖考试模块的实体。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class RecordExamStatVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    private Long examId;

    /** 交卷人数 */
    private Long submitCount = 0L;

    /** 及格人数 */
    private Long passCount = 0L;

    /** 及格率，百分数，保留一位小数 */
    private BigDecimal passRate = BigDecimal.ZERO;

    /** 平均分，保留一位小数 */
    private BigDecimal avgScore = BigDecimal.ZERO;

}
