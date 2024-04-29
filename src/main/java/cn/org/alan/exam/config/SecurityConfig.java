package cn.org.alan.exam.config;


import cn.org.alan.exam.filter.VerifyTokenFilter;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.util.ResponseUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Spring Security 权限配置
 *
 * @author haoxr
 * @since 2023/2/17
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Resource
    private ResponseUtil responseUtil;
    @Resource
    private VerifyTokenFilter verifyTokenFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(request -> {
            //放开认证
            request.requestMatchers("/api/auths/**").permitAll();
            request.requestMatchers("/api/auths/logout").authenticated();
            //所有请求的授权都需要认证
            request.anyRequest().authenticated();

        });

        //关闭表单功能，使用自定义登录和退出
        http.formLogin(AbstractHttpConfigurer::disable);

        // 配置拒绝访问处理器
        http.exceptionHandling(exceptionHandling -> exceptionHandling
                .accessDeniedHandler((request, response, accessDeniedException) ->
                        responseUtil.response(response, Result.failed("你没有该资源的访问权限"))));
        // 配置请求拦截前处理器，验证token
        http.addFilterBefore(verifyTokenFilter, UsernamePasswordAuthenticationFilter.class);
        // 放开跨域请求
        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * 不走过滤器链的放行配置
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(
//                        "/api/auths/**",
                        "/webjars/**",
                        "/doc.html",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/ws/**",
                        "/ws-app/**"
                );
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager 手动注入
     *
     * @param authenticationConfiguration 认证配置
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
