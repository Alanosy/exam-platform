package org.dromara.exam.stat.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 考试考生统计 stat_exam_user
 *
 * <p>一个考生 × 一次参考 一行。统计只认「最后一次」，
 * 由 {@code StatExamServiceImpl} 按 attemptNo 取最大那条落到汇总口径里。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stat_exam_user")
public class StatExamUser extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 已入统 */
    public static final String STAT_COUNTED = "COUNTED";
    /** 已交卷但有主观题未阅完，不计入统计 */
    public static final String STAT_PENDING_MARK = "PENDING_MARK";
    /** 管理员标记作废 */
    public static final String STAT_EXCLUDED = "EXCLUDED";

    private Long id;

    private Long examId;

    private Long userId;

    private Long recordId;

    private Integer attemptNo;

    private String account;

    private String userName;

    private String deptName;

    private BigDecimal totalScore;

    private BigDecimal objectiveScore;

    private BigDecimal subjectiveScore;

    private Integer correctCount;

    private Integer wrongCount;

    private Integer blankCount;

    private Integer passed;

    private Integer rankNo;

    private Integer usedSeconds;

    private Date submitTime;

    private String statStatus;
}
