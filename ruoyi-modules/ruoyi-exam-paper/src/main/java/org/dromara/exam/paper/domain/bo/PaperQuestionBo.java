package org.dromara.exam.paper.domain.bo;

import org.dromara.exam.paper.domain.PaperQuestion;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 试卷-试题中间业务对象 paper_question
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PaperQuestion.class, reverseConvertGenerate = false)
public class PaperQuestionBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 试卷ID
     */
    @NotNull(message = "试卷ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long paperId;

    /**
     * 试题ID，关联question表
     */
    @NotNull(message = "试题ID，关联question表不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long questionId;

    /**
     * 该题目在本试卷内分值，null使用question表默认score
     */
    private Long paperScore;

    /**
     * 题目在试卷中的排序
     */
    @NotNull(message = "题目在试卷中的排序不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long sort;


}
