package org.dromara.exam.answer.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 待阅主观题作答（交卷后同步给阅卷服务）
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class RemoteMarkAnswerBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试题ID */
    private Long questionId;

    /** 题型 */
    private String questionType;

    /** 题号顺序 */
    private Integer sort;

    /** 考生作答JSON */
    private String answerContent;

    /** 本题满分 */
    private BigDecimal fullScore;
}
