package org.dromara.exam.stat.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.exam.stat.domain.StatExamUser;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 考生成绩行
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@AutoMapper(target = StatExamUser.class)
public class StatUserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    /** COUNTED已入统 / PENDING_MARK待阅 / EXCLUDED作废 */
    private String statStatus;

    /** 超过百分之多少的考生，前端展示「超过 71%」 */
    private BigDecimal beatRate;

    /** 该题满分之和，用于算得分率 */
    private BigDecimal fullScore;
}
