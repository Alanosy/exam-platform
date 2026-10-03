package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 质检 / 审查发现的问题
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteAiIssueVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 严重度 fatal / major / minor */
    private String level;

    /** 问题类型 */
    private String type;

    /** 具体描述 */
    private String detail;

    /** 修改建议 */
    private String suggestion;
}
