package org.dromara.exam.manage.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 考试情况：整场考试的概览
 *
 * <p>「应考 / 参考 / 已交卷 / 待阅」这几个数是不同口径，别混：
 * - 应考：被安排了这场考试的人（白名单人数 或 已通过链接加入的人数）
 * - 参考：真正打开过试卷的人（有答卷记录的人数，按人去重）
 * - 已交卷：status = submitted 的答卷份数（同一人考两次算两份）
 * - 待阅：主观题还没阅完的答卷份数
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
public class ExamSituationOverviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 考试类型 1正式考试 2练习考试 */
    private String examType;

    /** 考试状态 not_start / ongoing / finished / archived */
    private String status;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    /** 考试限时（分钟） */
    private Long duration;

    /** 及格分，从第一份答卷记录上取（考试主表不存这个） */
    private BigDecimal passScore;

    /** 应考人数 */
    private Long invitedCount;

    /** 参考人数（去重后的考生数） */
    private Long joinedCount;

    /** 已交卷份数 */
    private Long submittedCount;

    /** 答题中份数（还没交卷） */
    private Long answeringCount;

    /** 待阅份数，大于 0 才需要给「去阅卷」的入口 */
    private Long pendingMarkCount;

    /** 平均分（只统计已交卷且成绩已确定的答卷） */
    private BigDecimal avgScore;

    /** 最高分 */
    private BigDecimal maxScore;

    /** 最低分 */
    private BigDecimal minScore;

    /** 及格人数（分子） */
    private Long passedCount;

    /** 及格率（%），分母是「成绩已确定」的答卷份数 */
    private BigDecimal passRate;
}
