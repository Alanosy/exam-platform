package org.dromara.exam.manage.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 白名单考生（管理后台展示用）
 *
 * <p>exam_user 里只存 userId，昵称 / 部门名在系统模块，这里由 dupbo 远程补全后一次性返回，
 * 管理端回显白名单时不必再去调用户接口。
 *
 * @author LionLi
 * @date 2026-10-02
 */
@Data
public class ExamWhiteUserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 考生用户ID
     */
    private Long userId;

    /**
     * 登录账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 所属部门名称
     */
    private String deptName;

    /**
     * 手机号码
     */
    private String phonenumber;

}
