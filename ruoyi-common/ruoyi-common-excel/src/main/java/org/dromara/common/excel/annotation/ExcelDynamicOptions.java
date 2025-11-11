package org.dromara.common.excel.annotation;

import java.lang.annotation.*;

/**
 * Excel动态下拉选项注解
 *
 * @author Angus
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelDynamicOptions {

    /**
     * 提供者类全限定名
     * 实现org.dromara.common.excel.service.ExcelOptionsProvider实现类接口
     */
    Class<?> providerClass();
}
