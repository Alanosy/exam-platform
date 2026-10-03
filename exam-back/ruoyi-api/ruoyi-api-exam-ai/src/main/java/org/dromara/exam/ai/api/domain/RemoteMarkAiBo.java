package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * AI 主观题评分入参
 *
 * <p>字段与阅卷服务的 MarkAiBo 一一对应，阅卷侧直接搬过来即可，
 * 不用在调用处做二次拼装。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteMarkAiBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试题ID（用于检索评分锚点，可空） */
    private Long questionId;

    /** 题型 SHORT_ANSWER / ESSAY / CODE 等 */
    private String questionType;

    /** 题干（纯文本，富文本调用前先脱标签） */
    private String title;

    /** 参考答案 */
    private String standardAnswer;

    /** 评分要点，没有就留空 */
    private String rubric;

    /** 试题解析 */
    private String analysis;

    /** 考生作答 */
    private String answerText;

    /** 本题满分 */
    private BigDecimal fullScore;

    /** 租户ID，用于隔离评分偏好记忆 */
    private String tenantId;
}
