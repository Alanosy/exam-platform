package org.dromara.common.doc.core.enhancer;

import io.swagger.v3.oas.models.Operation;
import org.dromara.common.doc.core.model.SaTokenSecurityMetadata;
import org.springframework.web.method.HandlerMethod;

/**
 * 权限元数据解析器接口
 *
 * @author echo
 */
public interface SaTokenMetadataResolver {

    /**
     * 解析权限元数据
     */
    void resolve(HandlerMethod handlerMethod, Operation operation, SaTokenSecurityMetadata metadata);

    /**
     * 获取解析器优先级
     */
    int getOrder();

    /**
     * 判断是否支持当前HandlerMethod
     */
    boolean supports(HandlerMethod handlerMethod);

    /**
     * 获取解析器的名称
     *
     * @return 解析器名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

}
