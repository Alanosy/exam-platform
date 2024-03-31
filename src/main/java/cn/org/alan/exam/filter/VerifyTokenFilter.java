package cn.org.alan.exam.filter;

import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.security.model.SysUserDetails;
import cn.org.alan.exam.util.JwtUtil;
import cn.org.alan.exam.util.ResponseUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/25 19:50
 */
@Component
public class VerifyTokenFilter extends OncePerRequestFilter {

    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private ResponseUtil responseUtil;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        //登录请求放行
        String uri = request.getRequestURI();
        if ("/login".equals(uri) || uri.contains("/druid")) {
            doFilter(request, response, filterChain);
            return;
        }
        //获取jwt令牌
        String authorization = request.getHeader("Authorization");
        //判断是否为空
        if (StringUtils.isBlank(authorization)) {
            responseUtil.response(response, Result.failed("Authorization为空，请先登录"));
            return;
        }

        //校验jwt是否过期
        boolean verify = jwtUtil.verifyToken(authorization);
        if (!verify) {
            responseUtil.response(response, Result.failed("token已过期，请重新登录"));
            return;
        }

        //验证token在redis中是否存在，k使用sessionId
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(request.getSession().getId() + "token"))) {
            responseUtil.response(response, Result.failed("token无效，请重新登录"));
            return;
        }

        //从jwt 获取用户信息和权限
        String userInfo = jwtUtil.getUser(authorization);
        List<String> authList = jwtUtil.getAuthList(authorization);
        //反序列化jwtToken获取用户信息
        User sysUser = objectMapper.readValue(userInfo, User.class);
        //权限转型
        List<SimpleGrantedAuthority> permissions = authList.stream().map(SimpleGrantedAuthority::new).toList();

        //创建登录用户
        SysUserDetails securityUser = new SysUserDetails(sysUser);
        securityUser.setPermissions(permissions);

        //创建权限授权的token 参数：用户，密码，权限 不给密码因为已经登录了
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(securityUser, null, permissions);
        //通过安全上下文设置授权token
        SecurityContextHolder.getContext().setAuthentication(token);
        //放行
        doFilter(request, response, filterChain);

    }


}
