package org.dromara.exam.question.domain.bo;

import org.dromara.exam.question.domain.QuestionBankCategory;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 题库分类目录业务对象 question_bank_category
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = QuestionBankCategory.class, reverseConvertGenerate = false)
public class QuestionBankCategoryBo extends BaseEntity {

    /**
     * 分类id
     */
    @NotNull(message = "分类id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 父分类id，0根节点
     */
    private Long parentId;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String categoryName;

    /**
     * 排序
     */
    private Long sort;

    /**
     *
     */
    private Long isDeleted;


}
