package org.dromara.gateway.filter;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.httpauth.basic.SaHttpBasicUtil;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.dev33.satoken.util.SaTokenConsts;
import org.dromara.common.core.constant.HttpStatus;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.gateway.config.properties.IgnoreWhiteProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * [Sa-Token 权限认证] 拦截器配置
 *
 * @author Lion Li
 */
@Configuration
public class AuthFilter implements WebMvcConfigurer {

    private final IgnoreWhiteProperties ignoreWhite;

    public AuthFilter(IgnoreWhiteProperties ignoreWhite) {
        this.ignoreWhite = ignoreWhite;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> SaRouter.match("/**")
            .notMatch(ignoreWhite.getWhites())
            .check(() -> {
                HttpServletRequest request = ServletUtils.getRequest();
                HttpServletResponse response = ServletUtils.getResponse();
                if (response != null) {
                    response.setContentType(SaTokenConsts.CONTENT_TYPE_APPLICATION_JSON);
                }

                StpUtil.checkLogin();

                String headerCid = request.getHeader(LoginHelper.CLIENT_KEY);
                String paramCid = ServletUtils.getParameter(LoginHelper.CLIENT_KEY);
                Object extra = StpUtil.getExtra(LoginHelper.CLIENT_KEY);
                String clientId = extra == null ? null : extra.toString();
                if (!StringUtils.equalsAny(clientId, headerCid, paramCid)) {
                    throw NotLoginException.newInstance(StpUtil.getLoginType(),
                        "-100", "客户端ID与Token不匹配",
                        StpUtil.getTokenValue());
                }
            })))
            .addPathPatterns("/**")
            .excludePathPatterns("/favicon.ico", "/actuator", "/actuator/**", "/resource/sse");
    }

    /**
     * 为 actuator 健康检查接口配置 Basic Auth 鉴权过滤器。
     *
     * @return Sa-Token Servlet 过滤器
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        String username = SpringUtils.getProperty("spring.cloud.nacos.discovery.metadata.username");
        String password = SpringUtils.getProperty("spring.cloud.nacos.discovery.metadata.userpassword");
        return new SaServletFilter()
            .addInclude("/actuator", "/actuator/**")
            .setAuth(obj -> {
                SaHttpBasicUtil.check(username + ":" + password);
            })
            .setError(e -> {
                HttpServletResponse response = ServletUtils.getResponse();
                response.setContentType(SaTokenConsts.CONTENT_TYPE_APPLICATION_JSON);
                return SaResult.error(e.getMessage()).setCode(HttpStatus.UNAUTHORIZED);
            });
    }

}
