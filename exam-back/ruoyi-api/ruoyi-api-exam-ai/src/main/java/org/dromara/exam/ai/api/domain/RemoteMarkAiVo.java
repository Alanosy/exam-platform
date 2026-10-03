package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * AI 主观题评分结果
 *
 * <p>score 是**建议分**，不是最终分。confidence 低于阈值（一般 0.6）
 * 时前端应提示「建议人工复核」，不要一键采纳。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteMarkAiVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否评估成功 */
    private Boolean success;

    /** AI 建议分，失败时为 null */
    private BigDecimal score;

    /** 评分理由，给教师看 */
    private String reason;

    /** 给学生的评语，可直接展示 */
    private String comment;

    /** 置信度 0-1 */
    private BigDecimal confidence;

    /** 是否需要人工复核 */
    private Boolean needHuman;

    /** 命中的评分要点 */
    private List<RemoteAiMatchPointVo> matchedPoints;

    /** 实际使用的模型 */
    private String model;

    /** 失败原因 */
    private String message;

    public static RemoteMarkAiVo fail(String message) {
        RemoteMarkAiVo vo = new RemoteMarkAiVo();
        vo.setSuccess(false);
        vo.setMessage(message);
        return vo;
    }
}
