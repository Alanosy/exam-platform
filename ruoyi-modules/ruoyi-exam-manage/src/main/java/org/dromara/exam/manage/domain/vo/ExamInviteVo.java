package org.dromara.exam.manage.domain.vo;

import java.util.Date;

import org.dromara.exam.manage.domain.ExamInvite;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 考试邀请记录视图对象 exam_invite
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ExamInvite.class)
public class ExamInviteVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 考试ID
     */
    @ExcelProperty(value = "考试ID")
    private Long examId;

    /**
     * 邀请账号：手机号/邮箱
     */
    @ExcelProperty(value = "邀请账号：手机号/邮箱")
    private String inviteAccount;

    /**
     * sms短信 / email邮件
     */
    @ExcelProperty(value = "sms短信 / email邮件")
    private String inviteType;

    /**
     * send已发送 / accept已进入考试 / expire已过期
     */
    @ExcelProperty(value = "send已发送 / accept已进入考试 / expire已过期")
    private String inviteStatus;

    /**
     * 邀请发送时间
     */
    @ExcelProperty(value = "邀请发送时间")
    private Date inviteTime;


}
