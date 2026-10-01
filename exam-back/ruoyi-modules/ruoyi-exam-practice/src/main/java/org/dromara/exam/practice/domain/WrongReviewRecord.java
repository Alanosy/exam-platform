package org.dromara.exam.practice.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 错题复习作答记录对象 wrong_review_record
 *
 * <p>每重做一次错题写一条，用于回看重做轨迹与掌握趋势。
 *
 * <p>同 WrongQuestion，本表只有业务列，不继承 BaseEntity 以免被注入不存在的审计列。
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Data
@TableName("wrong_review_record")
public class WrongReviewRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 错题记录ID
     */
    private Long userWrongId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 用户复习答案（与作答一致的JSON结构）
     */
    private String userAnswer;

    /**
     * 是否答对 0否 1是
     */
    private Integer isCorrect;

    /**
     * 复习时间
     */
    private Date reviewTime;

    /**
     * 租户ID
     */
    private String tenantId;

}
