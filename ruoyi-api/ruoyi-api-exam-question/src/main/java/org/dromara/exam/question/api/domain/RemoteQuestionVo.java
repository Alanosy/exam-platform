package org.dromara.exam.question.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 试题（跨服务传输用）
 *
 * <p>answer 字段是参考答案JSON，用于客观题自动判分，
 * 只在开考下发试卷时随题目一起给到答题服务，不直接暴露给考生端。
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class RemoteQuestionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试题ID */
    private Long questionId;

    /** 题干富文本 */
    private String title;

    /** 题型 SINGLE/MULTIPLE/JUDGE/BLANK/SHORT_ANSWER/ESSAY/CODE/UPLOAD_FILE/MATCH */
    private String questionType;

    /** 难度 easy / medium / hard */
    private String difficulty;

    /** 题目默认分值 */
    private BigDecimal score;

    /** 试题解析富文本 */
    private String analysis;

    /** 参考答案JSON */
    private String answer;

    /** 选项，客观题才有 */
    private List<RemoteQuestionOptionVo> options;

}
