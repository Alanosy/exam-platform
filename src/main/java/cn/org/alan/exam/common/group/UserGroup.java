package cn.org.alan.exam.common.group;

/**
 * 说明：
 * validation用户分组
 *
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/29 15:43
 */
public interface UserGroup {

    // 用户创建入参校验参数分组
    interface CreateUserGroup extends UserGroup {
    }

    // 用户修改密码入参校验分组
    interface UpdatePasswordGroup extends UserGroup {
    }

    interface RegisterGroup extends UserGroup {
    }
}
