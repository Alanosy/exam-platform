package org.dromara.exam.answer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
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

    /** 试卷名称 */
    private String paperName;

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
