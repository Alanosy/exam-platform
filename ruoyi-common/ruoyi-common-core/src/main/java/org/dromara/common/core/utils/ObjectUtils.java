package org.dromara.common.core.utils;

import cn.hutool.core.util.ObjectUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Function;

/**
 * 对象工具类
 *
 * @author 秋辞未寒
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectUtils extends ObjectUtil {

    /**
     * 如果对象不为空，则获取对象中的某个字段
     * <p>
     * 例：
     * <code>
     * <p>    public class User {
     * <p>        private String name;
     * <p>        // 省略 getter/setter
     * <p>    }
     * </code>
     * <code>
     * <p>    User user = userService.queryById(userId);
     * <p>    String name = ObjectUtils.notNullGetter(user,User::getName);
     * </code>
     * @param obj 对象
     * @param func 获取方法
     * @return 对象字段
     */
    public static <T,E> E notNullGetter(T obj, Function<T,E> func) {
        if (isNotNull(obj) && isNotNull(func)) {
            return func.apply(obj);
        }
        return null;
    }

}
