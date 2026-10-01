package org.dromara.exam.practice.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户错题集主对象 wrong_question
 *
 * <p>同一个用户同一道题只保留一条记录（uk_user_question），
 * 再次答错累加错误次数，复习做对累加答对次数。
 *
 * <p>这里刻意不继承 TenantEntity / BaseEntity：公共字段填充器会给 BaseEntity 子类
 * 注入 create_by / create_dept / update_by，而本表没有这三列，继承会导致插入报
 * Unknown column。租户ID由新增时显式写入，见 WrongQuestionServiceImpl。
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Data
@TableName("wrong_question")
public class WrongQuestion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 来源：正式考试 */
    public static final String SOURCE_EXAM = "EXAM";

    /** 来源：试卷练习 */
    public static final String SOURCE_PAPER_PRACTICE = "PAPER_PRACTICE";

    /** 掌握状态：未掌握 */
    public static final String MASTER_NOT = "NOT_MASTER";

    /** 掌握状态：已掌握 */
    public static final String MASTER_MASTERED = "MASTERED";

    /** 掌握状态：已忽略（用户移出错题本，可在「已忽略」里找回） */
    public static final String MASTER_IGNORED = "IGNORED";

    /** 连续答对达到该次数自动标记为已掌握 */
    public static final int AUTO_MASTER_RIGHT_COUNT = 2;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 系统用户ID（访客没有错题本）
     */
    private Long userId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 来源：EXAM正式考试 / PAPER_PRACTICE试卷练习
     */
    private String sourceType;

    /**
     * 来源ID：exam_id 或者 paper_id
     */
    private Long sourceId;

    /**
     * 首次答错对应的小题作答记录id（仅溯源）
     */
    private Long answerItemId;

    /**
     * 错误次数
     */
    private Integer wrongCount;

    /**
     * 连续答对次数（复习做对累加，答错归零）
     */
    private Integer rightCount;

    /**
     * 掌握状态：NOT_MASTER未掌握 / MASTERED已掌握 / IGNORED已忽略
     */
    private String masterStatus;

    /**
     * 用户笔记，富文本
     */
    private String userNote;

    /**
     * 最近一次答错时间
     */
    private Date lastWrongTime;

    /**
     * 最近一次复习时间
     */
    private Date lastReviewTime;

    /**
     * 进入错题本的时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 软删除 0未删 1已删
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 租户ID
     */
    private String tenantId;

}
