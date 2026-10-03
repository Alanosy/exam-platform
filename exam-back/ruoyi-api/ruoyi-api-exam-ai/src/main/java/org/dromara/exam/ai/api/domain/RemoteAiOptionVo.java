package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 题目选项
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteAiOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 选项键 A/B/C/D */
    private String key;

    /** 选项内容 */
    private String content;
}
