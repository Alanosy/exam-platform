package org.dromara.exam.question.domain.bo;

import org.dromara.exam.question.domain.KnowledgePoint;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 知识点业务对象 exam_knowledge_point
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = KnowledgePoint.class, reverseConvertGenerate = false)
public class KnowledgePointBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 父级ID，0 表示章节（根节点）
     */
    private Long parentId;

    /**
     * 知识点名称
     */
    @NotBlank(message = "知识点名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     * 排序
     */
    private Long sort;

    /**
     * 逻辑删除 0未删 1已删
     */
    private Long delFlag;

}
