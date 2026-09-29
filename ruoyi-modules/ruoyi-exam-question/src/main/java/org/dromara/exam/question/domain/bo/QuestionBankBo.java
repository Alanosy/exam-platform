package org.dromara.exam.question.domain.bo;

import org.dromara.exam.question.domain.QuestionBank;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 题库业务对象 question_bank
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = QuestionBank.class, reverseConvertGenerate = false)
public class QuestionBankBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 题库名称
     */
    @NotBlank(message = "题库名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bankName;

    /**
     * 题库描述
     */
    private String bankDesc;

    /**
     * 创建人用户ID
     */
    @NotNull(message = "创建人用户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long creatorId;

    /**
     * 可见性 private私有 / public公开
     */
    @NotBlank(message = "可见性 private私有 / public公开不能为空", groups = { AddGroup.class, EditGroup.class })
    private String visibility;

    /**
     * 状态 0草稿 1正常 2归档
     */
    @NotNull(message = "状态 0草稿 1正常 2归档不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long status;


}
