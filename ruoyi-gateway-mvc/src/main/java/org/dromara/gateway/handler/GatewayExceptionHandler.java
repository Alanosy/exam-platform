package org.dromara.gateway.handler;

import cn.dev33.satoken.exception.NotLoginException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.HttpStatus;
import org.dromara.common.core.domain.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * 网关统一异常处理
 *
 * @author Lion Li
 */
@Slf4j
@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLogin(HttpServletRequest request, NotLoginException ex) {
        log.warn("[网关认证失败]请求路径:{},异常信息:{}", request.getRequestURI(), ex.getMessage());
        return R.fail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    public R<Void> handle(HttpServletRequest request, Throwable ex) {
        String msg;
        if ("NotFoundException".equals(ex.getClass().getSimpleName())) {
            msg = "服务未找到";
        } else if (ex instanceof ResponseStatusException responseStatusException) {
            msg = responseStatusException.getMessage();
        } else {
            msg = "内部服务器错误";
        }

        log.error("[网关异常处理]请求路径:{},异常信息:{}", request.getRequestURI(), ex.getMessage(), ex);
        return R.fail(msg);
    }
}
