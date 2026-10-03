package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 试题质检结论
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteQuestionAuditVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否可用（质检成功且无 fatal 问题） */
    private Boolean success;

    /** 是否通过 */
    private Boolean passed;

    /** 质量分 0-10，>=8 视为可用 */
    private BigDecimal qualityScore;

    /** 问题清单 */
    private List<RemoteAiIssueVo> issues;

    /** 一句话结论 */
    private String summary;

    /** 使用的模型 */
    private String model;

    /** 失败原因 */
    private String message;

    public static RemoteQuestionAuditVo fail(String message) {
        RemoteQuestionAuditVo vo = new RemoteQuestionAuditVo();
        vo.setSuccess(false);
        vo.setMessage(message);
        return vo;
    }
}
