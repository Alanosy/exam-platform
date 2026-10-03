package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 薄弱知识点
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteAiWeakPointVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 知识点 */
    private String knowledgePoint;

    /** 错误次数 */
    private Integer wrongCount;

    /** 掌握度 0-1，越低越薄弱 */
    private BigDecimal mastery;

    /** 错误类型 concept / calculation / misread / incomplete / skill / careless */
    private String errorType;

    /** 判定依据 */
    private String evidence;
}
