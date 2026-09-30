package org.dromara.exam.manage.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 考试邀请记录（跨服务传输用）
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
public class RemoteExamInviteVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 考试ID */
    private Long examId;

    /** 邀请账号：手机号/邮箱/登录名 */
    private String inviteAccount;

    /** sms短信 / email邮件 / link链接 */
    private String inviteType;

    /** send已发送 / accept已进入考试 / expire已过期 */
    private String inviteStatus;

    /** 邀请时间 */
    private Date inviteTime;

}
