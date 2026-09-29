package org.dromara.exam.paper.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 试卷-试题中间对象 paper_question
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("paper_question")
public class PaperQuestion extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 试卷ID
     */
    private Long paperId;

    /**
     * 试题ID，关联question表
     */
    private Long questionId;

    /**
     * 该题目在本试卷内分值，null使用question表默认score
     */
    private Long paperScore;

    /**
     * 题目在试卷中的排序
     */
    private Long sort;

    /**
     * 逻辑删除 0未删 1已删
     */
    @TableLogic
    private Long delFlag;


}
