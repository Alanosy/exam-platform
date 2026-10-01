package org.dromara.exam.practice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 错题重做结果
 *
 * <p>重做完立刻告诉考生对错，并把正确答案与解析一并带回去。
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Data
public class WrongReviewResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错题记录ID
     */
    private Long wrongId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 本次是否答对
     */
    private Boolean correct;

    /**
     * 本次作答（人话）
     */
    private String myAnswerText;

    /**
     * 正确答案（人话）
     */
    private String standardAnswerText;

    /**
     * 试题解析富文本
     */
    private String analysis;

    /**
     * 本题选项，主观题为 null
     */
    private List<WrongOptionVo> options;

    /**
     * 更新后的连续答对次数
     */
    private Integer rightCount;

    /**
     * 更新后的错误次数
     */
    private Integer wrongCount;

    /**
     * 更新后的掌握状态
     */
    private String masterStatus;

    /**
     * 本次是否触发自动掌握
     */
    private Boolean autoMastered;

    /**
     * 提示语，答对时提示是否移出错题本
     */
    private String message;

}
