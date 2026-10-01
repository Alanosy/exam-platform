package org.dromara.exam.answer.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 单题阅卷得分
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class RemoteMarkScoreBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试题ID */
    private Long questionId;

    /** 本题得分 */
    private BigDecimal score;

    /** 0未判 1正确 2错误 */
    private Integer correct;
}
