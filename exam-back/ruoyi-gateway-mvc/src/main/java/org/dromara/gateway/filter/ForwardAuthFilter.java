package org.dromara.gateway.filter;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.same.SaSameUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.gateway.filter.support.MutableHttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 转发请求头过滤器:
 * 1. 转发内部 same-token
 *
 * @author Lion Li
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class ForwardAuthFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        MutableHttpServletRequest newRequest = null;

        if (SaManager.getConfig().getCheckSameToken()) {
            newRequest = getOrCreateMutableRequest(request, newRequest);
            newRequest.putHeader(SaSameUtil.SAME_TOKEN, SaSameUtil.getToken());
        }

        filterChain.doFilter(newRequest == null ? request : newRequest, response);
    }

    private MutableHttpServletRequest getOrCreateMutableRequest(HttpServletRequest request,
                                                                MutableHttpServletRequest currentRequest) {
        return currentRequest != null ? currentRequest : new MutableHttpServletRequest(request);
    }
}
