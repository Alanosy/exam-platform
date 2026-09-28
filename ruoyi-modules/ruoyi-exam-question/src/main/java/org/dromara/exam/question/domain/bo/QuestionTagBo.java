package org.dromara.exam.question.domain.bo;

import org.dromara.exam.question.domain.QuestionTag;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 试题标签业务对象 question_tag
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = QuestionTag.class, reverseConvertGenerate = false)
public class QuestionTagBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String tagName;

    /**
     * 创建人ID
     */
    @NotNull(message = "创建人ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long creatorId;


}
