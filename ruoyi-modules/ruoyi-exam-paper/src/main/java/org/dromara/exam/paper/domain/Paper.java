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
     * 随机抽题规则，paper_type=RANDOM时生效：{bankId,questionType,difficulty,count,scorePerQuestion}
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
     * 逻辑删除 0未删 1已删
     */
    @TableLogic
    private Long delFlag;


}
