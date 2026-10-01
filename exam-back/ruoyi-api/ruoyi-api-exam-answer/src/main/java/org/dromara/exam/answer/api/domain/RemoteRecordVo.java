package org.dromara.exam.answer.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 答卷记录（跨服务传输）
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class RemoteRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 答卷记录ID */
    private Long recordId;

    /** 考试ID */
    private Long examId;

    /** 试卷ID */
    private Long paperId;

    /** 考生用户ID */
    private Long userId;

    /** 考生账号 */
    private String account;

    /** 第几次参加 */
    private Integer attemptNo;

    /** answering答题中 / submitted已交卷 / expired超时作废 */
    private String status;

    /** 开考时间 */
    private Date startTime;

    /** 交卷时间 */
    private Date submitTime;

    /** 用时（秒） */
    private Integer usedSeconds;

    /** 题目总数 */
    private Integer questionCount;

    /** 已作答题目数 */
    private Integer answeredCount;

    /** 客观题得分 */
    private BigDecimal objectiveScore;

    /** 主观题得分 */
    private BigDecimal subjectiveScore;

    /** 总分 */
    private BigDecimal totalScore;

    /** 及格分 */
    private BigDecimal passScore;

    /** 是否及格 0否 1是 */
    private Long passed;

    /** 是否超时系统自动交卷 */
    private Long autoSubmit;
}
