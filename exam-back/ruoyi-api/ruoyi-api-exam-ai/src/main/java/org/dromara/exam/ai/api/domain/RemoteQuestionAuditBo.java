package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 试题质检入参
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteQuestionAuditBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 题型 */
    private String questionType;

    /** 难度标注 */
    private String difficulty;

    /** 知识点标注 */
    private List<String> knowledgePoints;

    /** 题干 */
    private String stem;

    /** 选项 */
    private List<RemoteAiOptionVo> options;

    /** 参考答案 */
    private String answer;

    /** 解析 */
    private String analysis;

    /** 审查侧重点（自然语言） */
    private String focus;

    /** 租户ID */
    private String tenantId;
}
