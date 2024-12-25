package org.dromara.common.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.web.config.properties.XssProperties;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.List;

/**
 * 防止XSS攻击的过滤器
 *
 * @author Lion Li
 */
public class XssFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (handleExcludeURL(req, resp)) {
            chain.doFilter(request, response);
            return;
        }
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper((HttpServletRequest) request);
        chain.doFilter(xssRequest, response);
    }

    private boolean handleExcludeURL(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getServletPath();
        String method = request.getMethod();
        // GET DELETE 不过滤
        if (method == null || HttpMethod.GET.matches(method) || HttpMethod.DELETE.matches(method)) {
            return true;
        }
        // 每次都获取处理 支持nacos热更配置
        XssProperties properties = SpringUtils.getBean(XssProperties.class);
        String prefix = StringUtils.blankToDefault(request.getHeader("X-Forwarded-Prefix"), "");
        // 从请求头获取gateway转发的服务前缀
        List<String> excludeUrls = properties.getExcludeUrls().stream()
            .filter(x -> StringUtils.startsWith(x, prefix))
            .map(x -> x.replaceFirst(prefix, StringUtils.EMPTY))
            .toList();
        return StringUtils.matches(url, excludeUrls);
    }

    @Override
    public void destroy() {

    }
}
