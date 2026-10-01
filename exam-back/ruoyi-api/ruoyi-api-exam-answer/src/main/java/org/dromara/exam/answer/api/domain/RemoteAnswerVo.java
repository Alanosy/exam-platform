package org.dromara.exam.answer.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 逐题作答（跨服务传输）
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class RemoteAnswerVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 作答明细ID */
    private Long answerId;

    /** 答卷记录ID */
    private Long recordId;

    /** 试题ID */
    private Long questionId;

    /** 题型 */
    private String questionType;

    /** 考生作答JSON */
    private String answerContent;

    /** 本题得分 */
    private BigDecimal score;

    /** 0未判 1正确 2错误 */
    private Integer correct;

    /** 题号顺序 */
    private Integer sort;
}
