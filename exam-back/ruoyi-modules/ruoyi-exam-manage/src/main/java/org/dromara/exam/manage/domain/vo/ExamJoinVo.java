package org.dromara.exam.manage.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 公开考试的加入信息 exam_join
 *
 * <p>考生用加入链接（/exam/join/{joinCode}）进入时看到的考试概要。
 * 这里刻意不返回 join_password 本身，只告诉前端「要不要输密码」，
 * 避免把参与密码暴露给任何拿到链接的人。
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class ExamJoinVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 考试ID
     */
    private Long examId;

    /**
     * 考试名称
     */
    private String examName;

    /**
     * 考试描述说明
     */
    private String examDesc;

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
     * not_start未开始 / ongoing进行中 / finished已结束 / archived归档
     */
    private String status;

    /**
     * 加入链接有效期，为空表示与考试结束时间一致
     */
    private Date joinExpireTime;

    /**
     * 是否需要参与密码
     */
    private Boolean needPassword;

    /**
     * 当前登录用户是否已加入本场考试
     */
    private Boolean joined;

    /**
     * 当前是否允许加入
     */
    private Boolean joinable;

    /**
     * 不允许加入时的原因，允许加入时为空
     */
    private String joinTip;

}
