package org.dromara.exam.stat.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.exam.stat.domain.StatExamSummary;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 考试统计列表行 / 概览
 *
 * <p>字段比 StatExamSummary 多一点：考试的进行状态和时间要从考试表带出来，
 * 前端列表要按状态着色、按时间排序。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@AutoMapper(target = StatExamSummary.class)
public class StatExamRowVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long examId;

    private Long paperId;

    private String examName;

    /** not_start / ongoing / finished / archived */
    private String examStatus;

    private String examType;

    private Date startTime;

    private Date endTime;

    private Integer invitedCount;

    private Integer submittedCount;

    /** 已入统人数，列表上真正该看的数 */
    private Integer countedCount;

    /** 待阅卷人数，>0 时前端整行标橙 */
    private Integer pendingMarkCount;

    private Integer excludedCount;

    private BigDecimal attendanceRate;

    private BigDecimal fullScore;

    private BigDecimal passScore;

    private BigDecimal maxScore;

    private BigDecimal minScore;

    private BigDecimal avgScore;

    private BigDecimal medianScore;

    private BigDecimal stdDev;

    private Integer passCount;

    private BigDecimal passRate;

    private Integer excellentCount;

    private BigDecimal excellentRate;

    private Integer avgUsedSeconds;

    private BigDecimal avgObjectiveScore;

    private BigDecimal avgSubjectiveScore;

    private BigDecimal difficulty;

    private BigDecimal discrimination;

    private String hasSubjective;

    private String partialScore;

    private Integer partialScoreRate;

    private String calcStatus;

    private Integer calcVersion;

    private Date calcTime;

    /** 数据可能不是最新：计算失败或长时间没重算 */
    private Boolean stale;
}
