package cn.org.alan.exam.config;


import cn.org.alan.exam.filter.VerifyTokenFilter;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.security.model.SysUserDetails;
import cn.org.alan.exam.util.JwtUtil;
import cn.org.alan.exam.util.ResponseUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests().requestMatchers("/login").permitAll();
        //所有请求的授权都需要认证
        http.authorizeRequests().anyRequest().authenticated();

        //配置登录成功处理器
        http.formLogin().successHandler((request, response, authentication) -> {
            //获取用户
            SysUserDetails principal = (SysUserDetails) authentication.getPrincipal();
            User sysUser = principal.getUser();
            //将用户信息转为字符串
            String userInfo = objectMapper.writeValueAsString(sysUser);
            //获取权限
            List<SimpleGrantedAuthority> permissions = (List<SimpleGrantedAuthority>) principal.getAuthorities();
            //将SimpleGrantedAuthority格式的权限转为String
            List<String> authList = permissions.stream().map(SimpleGrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            //将权限和用户信息封装到jwt中
            String token = jwtUtil.createJwt(userInfo, authList);
            //把token放到redis中
            stringRedisTemplate.opsForValue().set(request.getSession().getId() + "token", token, 2, TimeUnit.HOURS);
            //将token响应回前端
            responseUtil.response(response, Result.success("登录成功", token));
        });
        //配置登录失败处理器
        http.formLogin().failureHandler((request, response, exception) -> {
            responseUtil.response(response, Result.failed("用户或密码错误"));
        });
        //配置退出成功处理器
        http.logout().logoutSuccessHandler((request, response, authentication) -> {
            responseUtil.response(response, Result.success("退出成功"));
        });
        //配置拒绝访问处理器
        http.exceptionHandling().accessDeniedHandler((request, response, accessDeniedException) -> {
            responseUtil.response(response, Result.failed("你没有该资源的访问权限"));
        });
        //配置请求拦截前处理器，验证token
        http.addFilterBefore(verifyTokenFilter, UsernamePasswordAuthenticationFilter.class);

        http.csrf().disable();//放开跨域请求
        return http.build();
    }

    /**
     * 不走过滤器链的放行配置
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(
                        "/api/v1/auth/captcha",
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
