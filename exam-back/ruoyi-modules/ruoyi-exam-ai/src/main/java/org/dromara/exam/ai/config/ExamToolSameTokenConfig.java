package org.dromara.exam.ai.config;

import cn.dev33.satoken.same.SaSameUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Agent 直连通道的「内网通行证」
 *
 * <p>公共模块的 {@code SecurityConfiguration} 注册了一个覆盖 {@code /**} 的 SaServletFilter，
 * 它会校验 {@code SA-SAME-TOKEN}，这个头只有网关转发时才会带上（{@code ForwardAuthFilter}）。
 * 而 Python Agent 是**直连**本服务 9220 端口调 {@code /api/exam-tool/**} 的，
 * 没有这个头就会被挡在门外，报「认证失败，无法访问系统资源」。
 *
 * <p>这里只做一件事：给 {@code /api/exam-tool/**} 的请求补上通行证头。
 * 用 {@code SaSameUtil.getToken()} 取当前内网令牌（网关与所有服务共享同一个 Redis），
 * 不需要额外配置，也不会影响其它任何路径 —— 其它路径照旧必须经过网关。
 *
 * <p><b>前提</b>：这个通道本身由共享令牌 {@code X-Agent-Token} 把关（见 ExamToolController）。
 * 配了令牌就只给带正确令牌的请求补头；没配（本地联调）才对该路径放行。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Slf4j
@Configuration
public class ExamToolSameTokenConfig {

    /** Agent 工具端点路径前缀 */
    public static final String TOOL_PATH_PREFIX = "/api/exam-tool/";

    @Value("${exam-tool.token:}")
    private String toolToken;

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> examToolSameTokenFilter() {
        OncePerRequestFilter filter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                HttpServletRequest request, HttpServletResponse response, FilterChain chain
            ) throws ServletException, IOException {
                if (isAgentToolCall(request)) {
                    chain.doFilter(new SameTokenRequest(request), response);
                } else {
                    chain.doFilter(request, response);
                }
            }
        };
        FilterRegistrationBean<OncePerRequestFilter> bean = new FilterRegistrationBean<>(filter);
        // 必须排在公共模块那个 SaServletFilter 之前，否则头补上去时校验已经跑完了
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        bean.setName("examToolSameTokenFilter");
        return bean;
    }

    private boolean isAgentToolCall(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith(TOOL_PATH_PREFIX)) {
            return false;
        }
        // 没配共享令牌时放行：本地联调不想为了一次调用去改配置
        if (toolToken == null || toolToken.isEmpty()) {
            return true;
        }
        return toolToken.equals(request.getHeader("X-Agent-Token"));
    }

    /**
     * 只覆盖 {@code SA-SAME-TOKEN} 这一个头，其余原样透传
     */
    static class SameTokenRequest extends HttpServletRequestWrapper {

        SameTokenRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            if (SaSameUtil.SAME_TOKEN.equalsIgnoreCase(name)) {
                try {
                    String token = SaSameUtil.getToken();
                    if (token != null && !token.isEmpty()) {
                        return token;
                    }
                } catch (Exception e) {
                    log.warn("读取内网 same-token 失败: {}", e.getMessage());
                }
            }
            return super.getHeader(name);
        }
    }
}
