package org.dromara.exam.question.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 知识点对象 exam_knowledge_point
 *
 * <p>两级结构：{@code parentId = 0} 的是「章节」，其下挂「知识点」。
 * 全局共享（不挂题库），按租户隔离，供 AI 出题、组卷、错题归因、知识点统计共用。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_knowledge_point")
public class KnowledgePoint extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 父级ID，0 表示章节（根节点）
     */
    private Long parentId;

    /**
     * 知识点名称
     */
    private String name;

    /**
     * 排序
     */
    private Long sort;

    /**
     * 逻辑删除 0未删 1已删
     */
    @TableLogic
    private Long delFlag;

}
