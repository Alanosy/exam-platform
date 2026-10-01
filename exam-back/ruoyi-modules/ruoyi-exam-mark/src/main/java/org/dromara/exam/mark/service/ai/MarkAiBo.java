package org.dromara.exam.mark.service.ai;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * AI 阅卷入参
 *
 * <p>把判分需要的材料一次性备齐：题干、参考答案、解析、考生作答、满分。
 * 后续接 ruoyi-exam-ai 时直接把这个对象转成模型提示词即可。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class MarkAiBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试题ID */
    private Long questionId;

    /** 题型 */
    private String questionType;

    /** 题干（纯文本，富文本需先脱标签） */
    private String title;

    /** 参考答案 */
    private String standardAnswer;

    /** 试题解析 */
    private String analysis;

    /** 考生作答 */
    private String answerText;

    /** 本题满分 */
    private BigDecimal fullScore;
}
