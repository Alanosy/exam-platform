package org.dromara.exam.answer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 成绩详情里的单题结果
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class ExamResultQuestionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试题ID */
    private Long questionId;

    /** 题型 */
    private String questionType;

    /** 题干富文本 */
    private String title;

    /** 题号 */
    private Integer sort;

    /** 本题分值 */
    private BigDecimal score;

    /** 本题得分 */
    private BigDecimal gainedScore;

    /** 0未判 1正确 2错误 */
    private Integer correct;

    /** 我的作答JSON */
    private String myAnswer;

    /** 参考答案JSON，仅 showAnswer=true 时有值 */
    private String standardAnswer;

    /** 解析，仅 showAnswer=true 时有值 */
    private String analysis;

}
