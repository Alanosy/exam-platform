package org.dromara.exam.question.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 试题标签关联对象 question_tag_rel
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question_tag_rel")
public class QuestionTagRel extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 试题ID
     */
    private Long questionId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 逻辑删除 0未删 1已删
     */
    @TableLogic
    private Long delFlag;


}
