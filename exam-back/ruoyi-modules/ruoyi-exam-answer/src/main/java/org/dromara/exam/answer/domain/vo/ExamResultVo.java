package org.dromara.exam.answer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 交卷结果与成绩详情
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class ExamResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 答卷记录ID */
    private Long recordId;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 试卷ID */
    private Long paperId;

    /** 试卷名称 */
    private String paperName;

    /** 第几次参加，从1开始 */
    private Integer attemptNo;

    /** 开考时间 */
    private Date startTime;

    /** 交卷时间 */
    private Date submitTime;

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

    /** answering / submitted / expired */
    private String status;

    /** 客观题得分 */
    private BigDecimal objectiveScore;

    /** 主观题得分（人工阅卷后才有） */
    private BigDecimal subjectiveScore;

    /** 总分 */
    private BigDecimal totalScore;

    /** 试卷总分 */
    private BigDecimal paperTotalScore;

    /** 及格分 */
    private BigDecimal passScore;

    /** 是否及格 */
    private Boolean passed;

    /** 用时（秒） */
    private Integer usedSeconds;

    /** 是否超时自动交卷 */
    private Boolean autoSubmit;

    /** 是否展示答案与解析（受考试 show_answer_mode 控制） */
    private Boolean showAnswer;

    /** 每题结果，showAnswer=false 时不带正确答案与解析 */
    private List<ExamResultQuestionVo> questions;

}
