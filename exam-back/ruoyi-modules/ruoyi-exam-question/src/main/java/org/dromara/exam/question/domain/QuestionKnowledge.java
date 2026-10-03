package org.dromara.exam.question.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 试题-知识点关联对象 exam_question_knowledge
 *
 * <p>一道题可以挂多个知识点（多对多）。知识点改名 / 合并只动 exam_knowledge_point，
 * 不用刷题目数据，这也是没有把知识点名称直接写在 question 表上的原因。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_question_knowledge")
public class QuestionKnowledge extends TenantEntity {

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
     * 知识点ID
     */
    private Long knowledgeId;

}
