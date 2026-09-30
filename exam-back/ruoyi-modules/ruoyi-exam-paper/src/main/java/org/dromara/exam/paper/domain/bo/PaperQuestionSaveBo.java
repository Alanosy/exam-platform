package org.dromara.exam.paper.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 试卷试题保存对象（随试卷一并提交）
 *
 * <p>仅供「组卷保存」接口接收前端嵌套的试题明细使用，不参与单条 paper_question 的增删改查，
 * 因此不标注 {@code @AutoMapper}，避免污染 PaperQuestion 的转换关系。
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
public class PaperQuestionSaveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 试题ID，关联 question 表
     */
    @NotNull(message = "试题ID不能为空")
    private Long questionId;

    /**
     * 该题目在本试卷内的分值，为空则使用试卷的默认单题分值
     */
    private Long paperScore;

}
