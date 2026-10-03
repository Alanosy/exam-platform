package org.dromara.exam.question.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 试题-知识点关联视图对象
 *
 * <p>顺带把知识点名称带出来，列表页展示知识点就不用再查一次知识点表。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class QuestionKnowledgeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 试题ID
     */
    private Long questionId;

    /**
     * 知识点ID
     */
    private Long knowledgeId;

    /**
     * 知识点名称
     */
    private String knowledgeName;

}
