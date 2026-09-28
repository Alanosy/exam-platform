package org.dromara.exam.question.domain.bo;

import org.dromara.exam.question.domain.QuestionOption;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 试题选项业务对象 question_option
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = QuestionOption.class, reverseConvertGenerate = false)
public class QuestionOptionBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 试题ID
     */
    @NotNull(message = "试题ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long questionId;

    /**
     * 选项标识 A/B/C/D
     */
    @NotBlank(message = "选项标识 A/B/C/D不能为空", groups = { AddGroup.class, EditGroup.class })
    private String optionKey;

    /**
     * 选项内容富文本
     */
    @NotBlank(message = "选项内容富文本不能为空", groups = { AddGroup.class, EditGroup.class })
    private String optionContent;

    /**
     * 排序号
     */
    @NotNull(message = "排序号不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long sort;


}
