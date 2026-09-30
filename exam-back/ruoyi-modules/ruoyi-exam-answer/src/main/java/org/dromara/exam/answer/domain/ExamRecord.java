package org.dromara.exam.answer.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 考试答卷记录对象 exam_record
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_record")
public class ExamRecord extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 答题中 */
    public static final String STATUS_ANSWERING = "answering";

    /** 已交卷 */
    public static final String STATUS_SUBMITTED = "submitted";

    /** 超时作废 */
    public static final String STATUS_EXPIRED = "expired";

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 考试ID
     */
    private Long examId;

    /**
     * 试卷ID
     */
    private Long paperId;

    /**
     * 考生用户ID
     */
    private Long userId;

    /**
     * 考生账号（登录名）
     */
    private String account;

    /**
     * 第几次参加，从1开始
     */
    private Integer attemptNo;

    /**
     * answering答题中 / submitted已交卷 / expired超时作废
     */
    private String status;

    /**
     * 开考时间
     */
    private Date startTime;

    /**
     * 交卷时间
     */
    private Date submitTime;

    /**
     * 用时（秒）
     */
    private Integer usedSeconds;

    /**
     * 本场限时（分钟），0不限时
     */
    private Long durationMinutes;

    /**
     * 题目总数
     */
    private Integer questionCount;

    /**
     * 已作答题目数
     */
    private Integer answeredCount;

    /**
     * 客观题得分
     */
    private BigDecimal objectiveScore;

    /**
     * 主观题得分
     */
    private BigDecimal subjectiveScore;

    /**
     * 总分
     */
    private BigDecimal totalScore;

    /**
     * 及格分
     */
    private BigDecimal passScore;

    /**
     * 是否及格 0否 1是
     */
    private Long passed;

    /**
     * 是否超时自动交卷 0否 1是
     */
    private Long autoSubmit;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Long delFlag;


}
