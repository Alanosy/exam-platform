package cn.org.alan.exam.filter;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTPayload;
import cn.org.alan.exam.common.constant.SecurityConstants;
import cn.org.alan.exam.common.result.ResultCode;
import cn.org.alan.exam.common.util.ResponseUtils;
import cn.org.alan.exam.security.util.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * JWT token 校验过滤器
 *
 * @author haoxr
 * @since 2023/9/13
 */
public class JwtValidationFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;

    public JwtValidationFilter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 从请求中获取 JWT Token，校验 JWT Token 是否合法
     * <p>
     * 如果合法则将 Authentication 设置到 Spring Security Context 上下文中
     * 如果不合法则清空 Spring Security Context 上下文，并直接返回响应
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        // // try {
        // if (StrUtil.isNotBlank(token)) {
        //     Map<String, Object> payload = JwtUtils.parseToken(token);
        //
        //     String jti = Convert.toStr(payload.get(JWTPayload.JWT_ID));
        //     Boolean isTokenBlacklisted  = redisTemplate.hasKey(SecurityConstants.BLACKLIST_TOKEN_PREFIX + jti);
        //     if (Boolean.TRUE.equals(isTokenBlacklisted)) {
        //         ResponseUtils.writeErrMsg(response, ResultCode.TOKEN_INVALID);
        //         return;
        //     }
        //
        //     // Authentication authentication = JwtUtils.getAuthentication(payload);
        //     SecurityContextHolder.getContext().setAuthentication(authentication);
        // }
        // // } catch (BusinessException ex) {
        // //     //this is very important, since it guarantees the user is not authenticated at all
        // //     SecurityContextHolder.clearContext();
        // //     ResponseUtils.writeErrMsg(response, (ResultCode) ex.getResultCode());
        // //     return;
        // // }

        filterChain.doFilter(request, response);
    }
}
