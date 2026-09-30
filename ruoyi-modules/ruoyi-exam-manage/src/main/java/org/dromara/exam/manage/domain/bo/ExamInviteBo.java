package org.dromara.exam.manage.domain.bo;

import org.dromara.exam.manage.domain.ExamInvite;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.util.Date;

/**
 * 考试邀请记录业务对象 exam_invite
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ExamInvite.class, reverseConvertGenerate = false)
public class ExamInviteBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 考试ID
     */
    @NotNull(message = "考试ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long examId;

    /**
     * 邀请账号：手机号/邮箱
     */
    @NotBlank(message = "邀请账号：手机号/邮箱不能为空", groups = { AddGroup.class, EditGroup.class })
    private String inviteAccount;

    /**
     * sms短信 / email邮件
     */
    @NotBlank(message = "sms短信 / email邮件不能为空", groups = { AddGroup.class, EditGroup.class })
    private String inviteType;

    /**
     * send已发送 / accept已进入考试 / expire已过期
     */
    @NotBlank(message = "send已发送 / accept已进入考试 / expire已过期不能为空", groups = { AddGroup.class, EditGroup.class })
    private String inviteStatus;

    /**
     * 邀请发送时间
     */
    @NotNull(message = "邀请发送时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date inviteTime;


}
