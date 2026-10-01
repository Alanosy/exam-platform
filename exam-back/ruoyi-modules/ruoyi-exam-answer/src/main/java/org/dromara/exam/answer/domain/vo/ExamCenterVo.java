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

    /** 任务类型：1正式考试 / 2练习考试 */
    private String examType;

    /** 考试状态 not_start / ongoing / finished / archived */
    private String examStatus;

    /** 考试描述 */
    private String examDesc;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    /** 限时（分钟），0不限时 */
    private Long duration;

    /**
     * 服务端当前时间
     *
     * <p>前端的倒计时以它为准：考生电脑时钟不准时，按本地时间算的倒计时会提前或滞后，
     * 到点按钮不亮 / 亮了点进去被后端拒绝都很难受，这里把准绳一起下发。
     */
    private Date serverTime;

    /**
     * 最晚入场时间（开始时间 + 允许的迟到分钟 + 入场缓冲）
     *
     * <p>没有开始时间（练习考试）时为空，表示随时可入场。
     */
    private Date latestEntryTime;

    /** 考试状态 not_start / ongoing / finished / archived */
//    private String examStatus;

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

    /**
     * 是否是我创建的考试
     *
     * <p>自己建的考试不用走邀请链接，列表里也要能一眼认出来，
     * 前端据此打「我创建的」标签并支持按来源筛选。
     */
    private Boolean owner;

}
