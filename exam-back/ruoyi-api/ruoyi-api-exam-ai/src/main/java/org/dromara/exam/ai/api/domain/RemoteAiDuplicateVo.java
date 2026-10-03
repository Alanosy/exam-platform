package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 重复考查的题目对
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteAiDuplicateVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试题A */
    private Long questionA;

    /** 试题B */
    private Long questionB;

    /** 重复原因 */
    private String reason;
}
