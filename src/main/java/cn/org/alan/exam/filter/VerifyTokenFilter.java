package cn.org.alan.exam.filter;

import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.utils.security.SysUserDetails;
import cn.org.alan.exam.utils.JwtUtil;
import cn.org.alan.exam.utils.ResponseUtil;
import cn.org.alan.exam.common.result.Result;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Token校验过滤器
 *
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/25 19:50
 */
@Slf4j
@Component
public class VerifyTokenFilter extends OncePerRequestFilter {
    /**
     * JWT工具类
     */
    @Resource
    private JwtUtil jwtUtil;
    /**
     * Redis服务
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ResponseUtil responseUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 获取token
        String token = request.getHeader("Authorization");

        // 判断是否为空
        if (StringUtils.isBlank(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 去除 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // 从Redis中获取存储的JWT
        String sessionId = request.getSession().getId();
        String storedToken = stringRedisTemplate.opsForValue().get("token:" + sessionId);
        if (StringUtils.isBlank(storedToken) || !token.equals(storedToken)) {
            rejectIfProtected(request, response, filterChain);
            return;
        }
        // 验证并尝试续签 Token
        String refreshedToken = jwtUtil.verifyAndRefreshToken(token);
        if (refreshedToken == null) {
            rejectIfProtected(request, response, filterChain);
            return;
        }
        // 如果 Token 已续签，更新 Redis 中的 Token 并设置到响应头
        if (!refreshedToken.equals(token)) {
            stringRedisTemplate.opsForValue().set("token:" + request.getSession().getId(), refreshedToken, 30, TimeUnit.MINUTES);
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

    private void rejectIfProtected(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (isPublicRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        responseUtil.response(response, Result.failed("登录已过期，请重新登录"), 401);
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/auths/")
                || "/api/users/register".equals(uri)
                || "/api/user/info".equals(uri)
                || "/api/stat/allCounts".equals(uri)
                || uri.startsWith("/static/")
                || "/".equals(uri)
                || "/favicon.png".equals(uri)
                || uri.startsWith("/swagger")
                || uri.startsWith("/webjars/")
                || uri.startsWith("/v2/api-docs")
                || "/doc.html".equals(uri)
                || uri.startsWith("/ws/")
                || uri.startsWith("/ws-app/");
    }
}
