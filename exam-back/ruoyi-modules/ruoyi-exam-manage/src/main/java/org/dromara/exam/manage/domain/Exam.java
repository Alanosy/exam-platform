package org.dromara.exam.manage.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 考试主对象 exam
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam")
public class Exam extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 任务类型：正式考试 */
    public static final String TYPE_FORMAL = "1";

    /** 任务类型：练习考试 */
    public static final String TYPE_PRACTICE = "2";

    /**
     * 考试ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 考试名称
     */
    private String examName;

    /**
     * 考试描述说明
     */
    private String examDesc;

    /**
     * 关联试卷ID，paper表主键
     */
    private Long paperId;

    /**
     * 任务类型：1正式考试 / 2练习考试，取字典 exam_type
     *
     * <p>一场活动的规则全部挂在「考试」这一层，试卷只管题目与组卷属性。
     */
    private String examType;

    /**
     * 考试开始时间
     */
    private Date startTime;

    /**
     * 考试结束时间
     */
    private Date endTime;

    /**
     * 本场考试限时(分钟)，0不限时；限时属于活动规则，只在本场考试上配置
     */
    private Long duration;

    /**
     * 是否允许迟到入场 0否 1是
     */
    private Long allowLate;

    /**
     * 允许迟到多少分钟，超过无法进入
     */
    private Long lateMinute;

    /**
     * 是否允许重考 0否 1是
     */
    private Long allowRetry;

    /**
     * 单个考生最大重考次数
     */
    private Long maxRetryCount;

    /**
     * 答案展示 none不展示 / after_submit交卷后 / after_exam考试结束
     */
    private String showAnswerMode;

    /**
     * 防作弊配置：切屏次数、禁止复制粘贴、摄像头抓拍、全屏限制等
     */
    private String antiCheatConfig;

    /**
     * 考生准入类型 white白名单 / public公开链接
     */
    private String participantType;

    /**
     * 公开考试参与密码，public模式生效，为空无密码
     */
    private String joinPassword;

    /**
     * 公开考试链接有效期，NULL和考试结束时间一致
     */
    private Date joinExpireTime;

    /**
     * not_start未开始 / ongoing进行中 / finished已结束 / archived归档
     */
    private String status;

    /**
     * 公开考试的加入码，用于拼加入链接，participant_type=public 时才有值
     */
    private String joinCode;

    /**
     * 考试创建人ID
     */
    private Long creatorId;

    /**
     * 逻辑删除 0未删 1已删
     */
    @TableLogic
    private Long delFlag;


}
