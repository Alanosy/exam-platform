package org.dromara.gateway.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.gateway.server.mvc.filter.HttpHeadersFilter.RequestHttpHeadersFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.List;
import java.util.Map;

/**
 * Gateway MVC 下游请求头过滤器，补充服务访问前缀。
 *
 * @author Lion Li
 */
@Component
public class ForwardHeadersFilter implements RequestHttpHeadersFilter, Ordered {

    private static final String X_FORWARDED_PREFIX = "X-Forwarded-Prefix";

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public HttpHeaders apply(HttpHeaders input, ServerRequest request) {
        String forwardedPrefix = resolveForwardedPrefix(request.servletRequest());
        if (forwardedPrefix == null) {
            return input;
        }

        HttpHeaders headers = new HttpHeaders();
        for (Map.Entry<String, List<String>> entry : input.headerSet()) {
            headers.addAll(entry.getKey(), entry.getValue());
        }
        headers.set(X_FORWARDED_PREFIX, forwardedPrefix);
        return headers;
    }

    private String resolveForwardedPrefix(HttpServletRequest request) {
        String[] pathSegments = StringUtils.tokenizeToStringArray(request.getRequestURI(), "/");
        if (pathSegments.length == 0) {
            return null;
        }
        return "/" + pathSegments[0];
    }

}
