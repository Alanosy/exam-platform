package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 试卷审查结论
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemotePaperReviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 是否审查成功 */
    private Boolean success;

    /** 题目数 */
    private Integer questionCount;

    /** 难度分布 {"easy":6,"medium":10,"hard":4} */
    private String difficultyDistribution;

    /** 知识点覆盖情况 */
    private List<RemoteAiKnowledgeCoverageVo> knowledgeCoverage;

    /** 预计用时（分钟） */
    private Integer estimatedMinutes;

    /** 重复考查的题目对 */
    private List<RemoteAiDuplicateVo> duplicates;

    /** 问题清单 */
    private List<RemoteAiIssueVo> issues;

    /** 修改建议 */
    private List<String> suggestions;

    /** 结论：可直接使用 / 建议微调 / 需修改后使用 */
    private String verdict;

    /** 使用的模型 */
    private String model;

    /** 失败原因 */
    private String message;

    public static RemotePaperReviewVo fail(String message) {
        RemotePaperReviewVo vo = new RemotePaperReviewVo();
        vo.setSuccess(false);
        vo.setMessage(message);
        return vo;
    }
}
