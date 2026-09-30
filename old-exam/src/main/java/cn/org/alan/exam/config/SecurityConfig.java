package cn.org.alan.exam.config;

import cn.org.alan.exam.filter.VerifyTokenFilter;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.utils.ResponseUtil;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 权限配置类
 * 该类用于配置 Spring Security 的相关规则，包括请求授权、异常处理、过滤器等
 *
 * @author Alan
 * @since 2024/4/17
 */
@Configuration
@EnableWebSecurity // 启用 Spring Security 的 Web 安全功能，会自动配置一个过滤器链来处理安全相关的事务
@EnableGlobalMethodSecurity(prePostEnabled = true) // 启用全局方法级别的安全控制，允许使用 @PreAuthorize 和 @PostAuthorize 等注解进行方法级别的权限验证
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // 响应体封装工具类
    @Resource
    private ResponseUtil responseUtil;

    // 验证 token 的过滤器
    @Resource
    private VerifyTokenFilter verifyTokenFilter;

    /**
     * 配置 HttpSecurity 对象，定义请求的授权规则、异常处理方式、过滤器链等
     *
     * @param http HttpSecurity 对象，用于配置 Spring Security 的 HTTP 请求相关规则
     * @throws Exception 配置过程中可能出现的异常
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 开启 CORS（跨域资源共享）支持，并禁用 CSRF（跨站请求伪造）保护
        http.cors().and().csrf().disable();

        // 开始配置请求的授权规则
        http.authorizeRequests()
                // 定义一系列允许匿名访问（即无需身份验证即可访问）的请求路径
                .antMatchers(
                        // 用户登录相关的接口，例如登录、注册等接口
                        "/api/auths/**",
                        // Swagger2 相关的资源路径，用于提供 API 文档的访问
                        "/webjars/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/v2/api-docs",
                        "/swagger-resources/configuration/ui",
                        "/swagger-resources/configuration/security",
                        // 其他需要允许匿名访问的路径，如文档页面、WebSocket 相关路径等
                        "/doc.html",
                        "/ws/**",
                        "/ws-app/**"
                )
                // 这些路径允许所有请求访问，无需进行身份验证
                .permitAll()
                // 除了上述允许匿名访问的路径外，其他任何请求都必须经过身份验证才能访问
                .anyRequest().authenticated();

        // 配置异常处理器，当用户访问没有权限的资源时，会调用该处理器进行处理
        http.exceptionHandling()
                .accessDeniedHandler((request, response, accessDeniedException) ->
                        // 使用响应体封装工具类将错误信息封装成特定的结果返回给客户端
                        responseUtil.response(response, Result.failed("你没有该资源的访问权限"))
                );

        // 禁用 Spring Security 自带的基于表单的登录页面
        http.formLogin().disable();

        // 将自定义的验证 token 过滤器添加到 Spring Security 的过滤器链中，并且在 UsernamePasswordAuthenticationFilter 之前执行
        http.addFilterBefore(verifyTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }
}