package cn.org.alan.exam.security.exception;

import cn.org.alan.exam.common.result.ResultCode;
import cn.org.alan.exam.common.util.ResponseUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 认证异常处理
 *
 * @author haoxr
 * @since 2.0.0
 */
@Component
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {
        @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        int status = response.getStatus();
        if (status == HttpServletResponse.SC_NOT_FOUND) {
            // 资源不存在
            ResponseUtils.writeErrMsg(response, ResultCode.RESOURCE_NOT_FOUND);
        } else {

            if(authException instanceof BadCredentialsException){
                // 用户名或密码错误
                ResponseUtils.writeErrMsg(response, ResultCode.USERNAME_OR_PASSWORD_ERROR);
            }else {
                // 未认证或者token过期
                ResponseUtils.writeErrMsg(response, ResultCode.TOKEN_INVALID);
            }
        }
    }
}
