package cn.org.alan.exam.filter;
import org.springframework.beans.factory.annotation.Value;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.security.SysUserDetails;
import cn.org.alan.exam.util.impl.JwtUtil;
import cn.org.alan.exam.util.impl.ResponseUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/25 19:50
 */
@Slf4j
@Component
public class VerifyTokenFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private ResponseUtil responseUtil;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${system.register.enabled}")
    private boolean registerEnabled;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 登录、注册、校验验证码、获取验证码、放行
        String uri = request.getRequestURI();
        if (uri.contains("login") || uri.contains("verifyCode")
                || uri.contains("captcha")
                || (registerEnabled && uri.contains("register"))) {
            doFilter(request, response, filterChain);
            return;
        }
        // 获取token
        String token = request.getHeader("Authorization");
        // // 判断是否为空
        // if (StringUtils.isBlank(token)) {
        //     responseUtil.response(response, Result.failed("Authorization为空，请先登录"), 401);
        //     return;
        // }
        // // 校验jwt是否过期
        // boolean verify = jwtUtil.verifyToken(token);
        // if (!verify) {
        //     responseUtil.response(response, Result.failed("token已过期，请重新登录"), 401);
        //     return;
        // }
        // String userInfo = jwtUtil.getUser(token);
        // List<String> authList = jwtUtil.getAuthList(token);
        // // 反序列化jwtToken获取用户信息
        // User sysUser = objectMapper.readValue(userInfo, User.class);
        // // 权限转型
        // List<SimpleGrantedAuthority> permissions = authList.stream().map(SimpleGrantedAuthority::new).toList();
        // // 创建登录用户
        // SysUserDetails securityUser = new SysUserDetails(sysUser);
        // securityUser.setPermissions(permissions);
        // // 创建权限授权的token 参数：用户，密码，权限 不给密码因为已经登录了
        // UsernamePasswordAuthenticationToken token1 =
        //         new UsernamePasswordAuthenticationToken(securityUser, null, permissions);
        // // 通过安全上下文设置授权token
        // SecurityContextHolder.getContext().setAuthentication(token1);
        // 判断是否为空
        if (StringUtils.isBlank(token)) {
            responseUtil.response(response, Result.failed("Authorization为空，请先登录"), 401);
            return;
        }
        // 去除 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // 从Redis中获取存储的JWT
        String storedToken = stringRedisTemplate.opsForValue().get("token:" + request.getSession().getId());
        if (StringUtils.isBlank(storedToken) ||!token.equals(storedToken)) {
            responseUtil.response(response, Result.failed("token无效，请重新登录"), 401);
            return;
        }
        // 验证并尝试续签 Token
        String refreshedToken = jwtUtil.verifyAndRefreshToken(token);
        if (refreshedToken == null) {
            responseUtil.response(response, Result.failed("token无效或已过期，请重新登录"), 401);
            return;
        }
        // 如果 Token 已续签，更新 Redis 中的 Token 并设置到响应头
        if (!refreshedToken.equals(token)) {
            stringRedisTemplate.opsForValue().set("token" + request.getSession().getId(), refreshedToken, 30, TimeUnit.MINUTES);
            response.setHeader("Authorization", "Bearer " + refreshedToken);
        }

        // 从续签后的 Token 中获取用户信息和权限
        String userInfo = jwtUtil.getUser(refreshedToken);
        List<String> authList = jwtUtil.getAuthList(refreshedToken);

        // 反序列化 jwtToken 获取用户信息
        User sysUser = objectMapper.readValue(userInfo, User.class);

        // 权限转型
        List<SimpleGrantedAuthority> permissions = authList.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 创建登录用户
        SysUserDetails securityUser = new SysUserDetails(sysUser);
        securityUser.setPermissions(permissions);

        // 创建权限授权的 token 参数：用户，密码，权限 不给密码因为已经登录了
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(securityUser, null, permissions);

        // 通过安全上下文设置授权 token
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        doFilter(request, response, filterChain);
    }
}
