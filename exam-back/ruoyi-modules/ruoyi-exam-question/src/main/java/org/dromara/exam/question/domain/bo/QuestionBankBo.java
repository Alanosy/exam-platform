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
     * 创建人用户ID（新增时由后端自动填充当前登录用户，仅用于列表查询条件）
     */
    private Long creatorId;

    /**
     * 所属分类ID（传父级时会自动带上其下所有子分类）
     */
    private Long categoryId;

    /**
     * 可见性 private私有 / public公开
     */
    @NotBlank(message = "可见性 private私有 / public公开不能为空", groups = { AddGroup.class, EditGroup.class })
    private String visibility;

    /**
     * 状态 0草稿 1正常 2归档
     */
    @NotBlank(message = "状态 0草稿 1正常 2归档不能为空", groups = { AddGroup.class, EditGroup.class })
    private String  status;


}
