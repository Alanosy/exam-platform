package org.dromara.exam.stat.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 考试统计汇总 stat_exam_summary
 *
 * <p>一场考试一行，所有大盘与详情页的概览数字都从这里读，不实时扫答卷。
 * 由 {@code StatExamServiceImpl#recalc} 全量重算，calc_version 变更即使缓存失效。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stat_exam_summary")
public class StatExamSummary extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 计算中 */
    public static final String CALC_COMPUTING = "computing";
    /** 计算成功 */
    public static final String CALC_SUCCESS = "success";
    /** 计算失败 */
    public static final String CALC_FAIL = "fail";

    private Long id;

    private Long examId;

    private Long paperId;

    /** 考试名称快照，列表不用回查考试表 */
    private String examName;

    private Integer invitedCount;

    private Integer submittedCount;

    /** 已入统人数：真正进统计的那个数 */
    private Integer countedCount;

    /** 待阅卷人数：已交卷但有主观题没阅完，完全不计入统计 */
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

    /** 部分得分开关快照：统计口径要跟判分口径一致 */
    private String partialScore;

    private Integer partialScoreRate;

    private String calcStatus;

    private Integer calcVersion;

    private Date calcTime;
}
