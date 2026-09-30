package org.dromara.exam.answer.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 保存单题作答
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class AnswerSaveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试题ID */
    @NotNull(message = "试题ID不能为空")
    private Long questionId;

    /**
     * 作答内容JSON
     * 客观题：{choices:["A"]} / {blanks:["答案"]}
     * 主观题：{text:"..."}
     */
    @NotBlank(message = "作答内容不能为空")
    private String answerContent;

}
