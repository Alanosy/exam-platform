package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * AI 出题入参
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteQuestionGenBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 题型 SINGLE / MULTIPLE / JUDGE / BLANK / SHORT_ANSWER / ESSAY / CODE */
    private String questionType;

    /** 难度 easy / medium / hard */
    private String difficulty;

    /** 知识点 */
    private List<String> knowledgePoints;

    /** 生成数量，1-30 */
    private Integer count;

    /** 默认分值 */
    private BigDecimal score;

    /** 附加要求（自然语言） */
    private String extra;

    /** 参考资料（RAG 命中的课件片段），没有就留空 */
    private String ragContext;

    /** 是否同时做质检，true 时只返回通过质检的题目 */
    private Boolean withAudit;

    /** 租户ID */
    private String tenantId;
}
