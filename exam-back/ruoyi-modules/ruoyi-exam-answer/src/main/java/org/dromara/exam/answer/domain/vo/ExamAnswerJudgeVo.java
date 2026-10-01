package org.dromara.exam.answer.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 刷题即时判题结果
 *
 * <p>练习考试开了「即时判题」后，考生每答一题就把这一题的判分结果、正确答案、
 * 解析一次性回给他，不用等交卷。主观题没法自动判分，correct 为 null。
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class ExamAnswerJudgeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试题ID */
    private Long questionId;

    /** 本次是否答对，主观题为 null（无法自动判分） */
    private Boolean correct;

    /** 本次作答，人话版 */
    private String myAnswerText;

    /** 正确答案，人话版 */
    private String standardAnswerText;

    /** 试题解析富文本 */
    private String analysis;

    /** 提示语 */
    private String message;

}
