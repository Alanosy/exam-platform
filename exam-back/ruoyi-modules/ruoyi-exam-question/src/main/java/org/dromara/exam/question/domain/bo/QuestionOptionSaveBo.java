package org.dromara.exam.question.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 试题选项保存对象（随试题一并提交）
 *
 * <p>仅供「新增试题」接口接收前端嵌套的选项数据使用，不参与单个选项的增删改查，
 * 因此不标注 {@code @AutoMapper}，避免污染 QuestionOption 的转换关系。
 *
 * <p>注意：question_option 表没有「是否正确答案」列，正确答案统一由 question.answer
 * 的 JSON 描述（如 {@code {"rightKeys":["A"]}}）。这里的 isRight 仅用于前端未传 answer 时，
 * 由后端反推正确答案。
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
public class QuestionOptionSaveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，编辑已存在的选项时携带
     */
    private Long id;

    /**
     * 选项标识 A/B/C/D
     */
    @NotBlank(message = "选项标识 A/B/C/D不能为空")
    private String optionKey;

    /**
     * 选项内容富文本
     */
    @NotBlank(message = "选项内容不能为空")
    private String optionContent;

    /**
     * 排序号
     */
    private Long sort;

    /**
     * 是否为正确答案（不落库，用于推导 question.answer）
     */
    private Boolean isRight;

}
