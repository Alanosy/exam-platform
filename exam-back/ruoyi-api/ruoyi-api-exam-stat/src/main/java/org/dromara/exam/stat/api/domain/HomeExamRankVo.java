package org.dromara.exam.stat.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 单场考试的成绩表现（首页热度榜）
 *
 * <p>考试名字段由统计服务回查考试表后回填 —— 考试被删了也不影响这条统计，只是没名字。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class HomeExamRankVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    private Long examId;

    /** 考试名称，查不到时为空 */
    private String examName;

    /** 交卷人数 */
    private Long submitCount = 0L;

    /** 及格人数 */
    private Long passCount = 0L;

    /** 及格率，百分数，保留一位小数 */
    private BigDecimal passRate = BigDecimal.ZERO;

    /** 平均分，保留一位小数 */
    private BigDecimal avgScore = BigDecimal.ZERO;

}
