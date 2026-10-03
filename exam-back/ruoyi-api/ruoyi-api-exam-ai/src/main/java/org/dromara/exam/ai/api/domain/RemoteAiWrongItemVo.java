package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 错题摘要
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteAiWrongItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试题ID */
    private Long questionId;

    /** 题型 */
    private String questionType;

    /** 题干 */
    private String stem;

    /** 知识点 */
    private List<String> knowledgePoints;

    /** 考生作答 */
    private String answerText;

    /** 正确答案 */
    private String standardAnswer;

    /** 错误次数 */
    private Integer wrongCount;
}
