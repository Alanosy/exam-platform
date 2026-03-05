package org.dromara.common.doc.core.enhancer;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.convert.Convert;
import io.swagger.v3.oas.models.Operation;
import org.dromara.common.doc.core.model.SaTokenSecurityMetadata;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;

/**
 * 基于JavaDoc的SaToken权限解析器
 *
 * @author echo
 */
public class SaTokenJavadocResolver implements SaTokenMetadataResolver {

    public static final Class<SaCheckRole> SA_CHECK_ROLE_CLASS = SaCheckRole.class;
    public static final Class<SaCheckPermission> SA_CHECK_PERMISSION_CLASS = SaCheckPermission.class;
    public static final Class<SaIgnore> SA_IGNORE_CLASS = SaIgnore.class;
    public static final Class<SaCheckLogin> SA_CHECK_LOGIN = SaCheckLogin.class;

    /**
     * 核心解析方法
     */
    @Override
    public void resolve(HandlerMethod handlerMethod, Operation operation, SaTokenSecurityMetadata metadata) {
        // 检查是否忽略校验
        if (isIgnore(handlerMethod)) {
            metadata.setIgnore(true);
            return;
        }

        // 解析权限校验
        resolvePermissionCheck(handlerMethod, metadata);

        // 解析角色校验
        resolveRoleCheck(handlerMethod, metadata);
    }

    /**
     * 解析器优先级
     */
    @Override
    public int getOrder() {
        return 100;
    }

    /**
     * 判断是否支持当前HandlerMethod
     */
    @Override
    public boolean supports(HandlerMethod handlerMethod) {
        return hasAnnotation(handlerMethod
            .getMethodAnnotation(SA_CHECK_PERMISSION_CLASS)) || hasAnnotation(handlerMethod
            .getMethodAnnotation(SA_CHECK_ROLE_CLASS)) || hasAnnotation(handlerMethod
            .getMethodAnnotation(SA_IGNORE_CLASS)) || hasAnnotation(handlerMethod
            .getBeanType()
            .getAnnotation(SA_CHECK_PERMISSION_CLASS)) || hasAnnotation(handlerMethod
            .getBeanType()
            .getAnnotation(SA_CHECK_ROLE_CLASS)) || hasAnnotation(handlerMethod
            .getBeanType()
            .getAnnotation(SA_IGNORE_CLASS));
    }

    @Override
    public String getName() {
        return "SaTokenJavadocResolver";
    }

    /**
     * 检查是否忽略校验
     */
    private boolean isIgnore(HandlerMethod handlerMethod) {
        // 检查方法上的注解
        if (hasAnnotation(handlerMethod.getMethodAnnotation(SA_IGNORE_CLASS))) {
            return true;
        }
        // 检查类上的注解
        return hasAnnotation(handlerMethod.getBeanType().getAnnotation(SA_IGNORE_CLASS));
    }

    /**
     * 解析权限校验
     */
    private void resolvePermissionCheck(HandlerMethod handlerMethod, SaTokenSecurityMetadata metadata) {
        // 获取方法上的注解
        Annotation methodAnnotation = handlerMethod
            .getMethodAnnotation(SA_CHECK_PERMISSION_CLASS);
        // 获取类上的注解
        Annotation classAnnotation = handlerMethod.getBeanType()
            .getAnnotation(SA_CHECK_PERMISSION_CLASS);

        // 解析权限信息
        if (hasAnnotation(methodAnnotation)) {
            resolvePermissionAnnotation(metadata, methodAnnotation);
        }
        if (hasAnnotation(classAnnotation)) {
            resolvePermissionAnnotation(metadata, classAnnotation);
        }
    }

    /**
     * 解析权限注解
     */
    private void resolvePermissionAnnotation(SaTokenSecurityMetadata metadata, Annotation annotation) {
        try {
            // 反射获取注解属性
            Object value = getAnnotationValue(annotation, "value");
            Object mode = getAnnotationValue(annotation, "mode");
            Object type = getAnnotationValue(annotation, "type");
            Object orRole = getAnnotationValue(annotation, "orRole");

            String[] values = Convert.toStrArray(value);
            String modeStr = mode != null ? mode.toString() : "AND";
            String typeStr = type != null ? type.toString() : "";
            String[] orRoles = Convert.toStrArray(orRole);

            metadata.addPermission(values, modeStr, typeStr, orRoles);
        } catch (Exception ignore) {
            // 忽略解析错误
        }
    }

    /**
     * 解析角色校验
     */
    private void resolveRoleCheck(HandlerMethod handlerMethod, SaTokenSecurityMetadata metadata) {
        // 获取方法上的注解
        Annotation methodAnnotation = handlerMethod.getMethodAnnotation(SA_CHECK_ROLE_CLASS);
        // 获取类上的注解
        Annotation classAnnotation = handlerMethod.getBeanType()
            .getAnnotation(SA_CHECK_ROLE_CLASS);

        // 解析角色信息
        if (hasAnnotation(methodAnnotation)) {
            resolveRoleAnnotation(metadata, methodAnnotation);
        }
        if (hasAnnotation(classAnnotation)) {
            resolveRoleAnnotation(metadata, classAnnotation);
        }
    }

    /**
     * 解析角色注解
     */
    private void resolveRoleAnnotation(SaTokenSecurityMetadata metadata, Annotation annotation) {
        try {
            // 反射获取注解属性
            Object value = getAnnotationValue(annotation, "value");
            Object mode = getAnnotationValue(annotation, "mode");
            Object type = getAnnotationValue(annotation, "type");

            String[] values = Convert.toStrArray(value);
            String modeStr = mode != null ? mode.toString() : "AND";
            String typeStr = type != null ? type.toString() : "";

            metadata.addRole(values, modeStr, typeStr);
        } catch (Exception ignore) {
            // 忽略解析错误
        }
    }

    /**
     * 检查注解是否存在
     */
    private boolean hasAnnotation(Annotation annotation) {
        return annotation != null;
    }

    /**
     * 获取注解属性值
     */
    private Object getAnnotationValue(Annotation annotation, String attributeName) {
        try {
            return annotation.annotationType().getMethod(attributeName).invoke(annotation);
        } catch (Exception e) {
            return null;
        }
    }

}
