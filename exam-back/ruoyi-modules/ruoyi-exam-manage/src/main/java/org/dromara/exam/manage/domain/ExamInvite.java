package org.dromara.exam.manage.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 考试邀请记录对象 exam_invite
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_invite")
public class ExamInvite extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 考试ID
     */
    private Long examId;

    /**
     * 邀请账号：手机号/邮箱
     */
    private String inviteAccount;

    /**
     * sms短信 / email邮件
     */
    private String inviteType;

    /**
     * send已发送 / accept已进入考试 / expire已过期
     */
    private String inviteStatus;

    /**
     * 邀请发送时间
     */
    private Date inviteTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Long delFlag;


}
