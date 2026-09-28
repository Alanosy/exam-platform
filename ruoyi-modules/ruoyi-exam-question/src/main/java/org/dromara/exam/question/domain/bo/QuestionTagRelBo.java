package org.dromara.exam.question.domain.bo;

import org.dromara.exam.question.domain.QuestionTagRel;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 试题标签关联业务对象 question_tag_rel
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = QuestionTagRel.class, reverseConvertGenerate = false)
public class QuestionTagRelBo extends BaseEntity {

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
     * 标签ID
     */
    @NotNull(message = "标签ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long tagId;


}
