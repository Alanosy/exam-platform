package org.dromara.exam.practice.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 错题重做历史
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Data
public class WrongReviewRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 错题记录ID
     */
    private Long userWrongId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 本次作答（人话）
     */
    private String userAnswerText;

    /**
     * 是否答对
     */
    private Boolean correct;

    /**
     * 复习时间
     */
    private Date reviewTime;

}
