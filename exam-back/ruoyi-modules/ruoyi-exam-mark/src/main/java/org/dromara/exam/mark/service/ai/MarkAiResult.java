package org.dromara.exam.mark.service.ai;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * AI 阅卷结果
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class MarkAiResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否评估成功 */
    private Boolean success;

    /** AI 建议分，失败时为 null */
    private BigDecimal score;

    /** 评分理由，给教师看的 */
    private String reason;

    /** 模型标识 */
    private String model;

    /** 失败原因 */
    private String message;

    public static MarkAiResult fail(String message) {
        MarkAiResult result = new MarkAiResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    public static MarkAiResult ok(BigDecimal score, String reason, String model) {
        MarkAiResult result = new MarkAiResult();
        result.setSuccess(true);
        result.setScore(score);
        result.setReason(reason);
        result.setModel(model);
        return result;
    }
}
