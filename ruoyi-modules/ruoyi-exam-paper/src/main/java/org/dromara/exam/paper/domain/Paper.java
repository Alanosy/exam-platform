package org.dromara.exam.paper.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 试卷主对象 paper
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("paper")
public class Paper extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 试卷主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 试卷名称
     */
    private String paperName;

    /**
     * 试卷描述
     */
    private String paperDesc;

    /**
     * 组卷模式 MANUAL手动选题 / RANDOM随机抽题(AI抽题)
     */
    private String paperType;

    /**
     * 试卷总分
     */
    private Long totalScore;

    /**
     * 及格分数
     */
    private Long passScore;

    /**
     * 考试时长(分钟)，0代表不限时
     */
    private Long timeLimit;

    /**
     * 可见性 private私有 / public公开
     */
    private String visibility;

    /**
     * 公开分享密码，公开模式生效，空则无密码
     */
    private String sharePassword;

    /**
     * 分享链接过期时间，NULL永久有效
     */
    private Date shareExpireTime;

    /**
     * 随机抽题规则(JSON数组)，paper_type=RANDOM时生效，用于重新抽题：
     * [{bankId,questionType,difficulty,count,score}]
     */
    private String randomRule;

    /**
     * draft草稿 / ready已组卷 / archived归档
     */
    private String status;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 试卷分类，取字典 paper_category 的字典值
     */
    private String category;

    /**
     * 默认单题分值，手动选题未单独指定分值时使用
     */
    private Long defaultScore;

    /**
     * 是否开启题目乱序 0否 1是
     */
    private String questionShuffle;

    /**
     * 是否开启选项乱序 0否 1是
     */
    private String optionShuffle;

    /**
     * 客观题是否自动判分 0否 1是
     */
    private String autoJudge;

    /**
     * 主观题是否人工阅卷 0否 1是
     */
    private String manualReview;

    /**
     * 是否支持部分得分 0否 1是
     */
    private String partialScore;

    /**
     * 答错是否扣分 0否 1是
     */
    private String wrongDeduct;

    /**
     * 可见范围 SELF仅自己可编辑 / SHARED共享给其他管理员
     */
    private String shareScope;

    /**
     * 逻辑删除 0未删 1已删
     */
    @TableLogic
    private Long delFlag;


}
