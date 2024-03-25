package cn.org.alan.exam.common.constant;

/**
 * 缓存常量
 *
 * @author haoxr
 * @since 2023/11/24
 */
public interface SecurityConstants {

    /**
     * 验证码缓存前缀
     */
    String CAPTCHA_CODE_PREFIX = "captcha_code:";

    /**
     * 角色和权限缓存前缀
     */
    String ROLE_PERMS_PREFIX = "role_perms:";

    /**
     * 黑名单Token缓存前缀
     */
    String BLACKLIST_TOKEN_PREFIX = "token:blacklist:";


}
