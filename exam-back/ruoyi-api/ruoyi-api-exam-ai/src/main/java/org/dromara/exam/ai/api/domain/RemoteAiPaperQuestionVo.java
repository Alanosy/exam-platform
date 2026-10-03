package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 试卷审查用的题目摘要
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteAiPaperQuestionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试题ID */
    private Long questionId;

    /** 题型 */
    private String questionType;

    /** 题干 */
    private String stem;

    /** 难度 */
    private String difficulty;

    /** 知识点 */
    private List<String> knowledgePoints;

    /** 分值 */
    private BigDecimal score;
}
