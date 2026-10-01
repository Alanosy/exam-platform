package org.dromara.exam.mark.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 待阅主观题（答题服务交卷后同步过来）
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
public class RemoteMarkItemSyncBo implements Serializable {

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
