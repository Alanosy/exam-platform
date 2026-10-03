package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 试卷审查入参
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemotePaperReviewBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 试卷ID */
    private Long paperId;

    /** 试卷标题 */
    private String title;

    /** 考试时长（分钟） */
    private Integer duration;

    /** 试卷总分 */
    private BigDecimal totalScore;

    /** 及格分 */
    private BigDecimal passScore;

    /** 题目清单 */
    private List<RemoteAiPaperQuestionVo> questions;

    /** 审查侧重点（自然语言） */
    private String focus;

    /** 租户ID */
    private String tenantId;
}
