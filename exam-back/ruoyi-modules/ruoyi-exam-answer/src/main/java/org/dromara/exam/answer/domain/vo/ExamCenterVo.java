package org.dromara.exam.answer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 考试中心：我的考试卡片
 *
 * <p>一张卡片 = 一场我参与过的考试，卡片上直接给出当前该做什么：
 * 未开始 / 去考试 / 继续答题 / 已交卷看成绩 / 已结束。
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class ExamCenterVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 考试描述 */
    private String examDesc;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    /** 限时（分钟），0不限时 */
    private Long duration;

    /** 考试状态 not_start / ongoing / finished / archived */
    private String examStatus;

    /**
     * 我在这场比赛上的状态：
     * not_start未开始 / pending待考试 / answering答题中 / submitted已交卷 /
     * ended考试已结束 / late迟到不可参加 / blocked不可参加
     */
    private String myStatus;

    /** 不可参加时的原因，可参加时为空 */
    private String tip;

    /** 当前进行中或最近一次的答卷ID，没有时为空 */
    private Long recordId;

    /** 已参加次数 */
    private Integer attemptCount;

    /** 最近一次成绩，未出成绩时为空 */
    private BigDecimal totalScore;

    /** 及格分 */
    private BigDecimal passScore;

    /** 是否及格，未出成绩时为 null */
    private Boolean passed;

    /** 是否可以开始/继续考试 */
    private Boolean canStart;

}
