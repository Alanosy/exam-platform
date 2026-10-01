package org.dromara.exam.answer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 考试记录（我的每一次考试）
 *
 * <p>列表页一行就是一次答卷：哪场考试、第几次、什么时候考的、得了多少分。
 * 考试名与试卷名不在本服务库里，通过 Dubbo 从 manage / paper 服务取回后拼上。
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class ExamRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 答卷记录ID */
    private Long recordId;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 任务类型：1正式考试 / 2练习考试，列表要据此区分「考试记录」与「练习记录」 */
    private String examType;

    /** 试卷ID */
    private Long paperId;

    /** 试卷名称 */
    private String paperName;

    /** 第几次参加，从1开始 */
    private Integer attemptNo;

    /** answering答题中 / submitted已交卷 / expired超时作废 */
    private String status;

    /** 开考时间 */
    private Date startTime;

    /** 交卷时间 */
    private Date submitTime;

    /** 用时（秒） */
    private Integer usedSeconds;

    /** 本场限时（分钟），0不限时 */
    private Long durationMinutes;

    /** 题目总数 */
    private Integer questionCount;

    /** 已作答题目数 */
    private Integer answeredCount;

    /** 判对的题数 */
    private Integer correctCount;

    /** 判错的题数 */
    private Integer wrongCount;

    /** 客观题得分 */
    private BigDecimal objectiveScore;

    /** 主观题得分 */
    private BigDecimal subjectiveScore;

    /** 总分 */
    private BigDecimal totalScore;

    /** 试卷总分 */
    private BigDecimal paperTotalScore;

    /** 及格分 */
    private BigDecimal passScore;

    /** 是否及格 */
    private Boolean passed;

    /** 是否超时自动交卷 */
    private Boolean autoSubmit;

    /** 该考试当前是否允许看答案与解析 */
    private Boolean showAnswer;

}
