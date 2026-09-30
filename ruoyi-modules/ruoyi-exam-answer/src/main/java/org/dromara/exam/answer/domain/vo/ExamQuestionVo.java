package org.dromara.exam.answer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 答题页的题目
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class ExamQuestionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试题ID */
    private Long questionId;

    /** 题型 SINGLE/MULTIPLE/JUDGE/BLANK/SHORT_ANSWER/ESSAY/CODE/UPLOAD_FILE/MATCH */
    private String questionType;

    /** 题干富文本 */
    private String title;

    /** 本题分值 */
    private BigDecimal score;

    /** 题号，从1开始 */
    private Integer sort;

    /** 选项，客观题才有 */
    private List<ExamOptionVo> options;

    /** 我已作答的内容（JSON），未作答时为空 */
    private String myAnswer;

}
