package org.dromara.gateway.filter;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.gateway.config.properties.ApiDecryptProperties;
import org.dromara.gateway.config.properties.CustomGatewayProperties;
import org.dromara.gateway.filter.support.CachedBodyHttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 全局日志过滤器
 * <p>
 * 用于打印请求执行参数与响应时间等等
 *
 * @author Lion Li
 */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class GlobalLogFilter extends OncePerRequestFilter {

    private final CustomGatewayProperties customGatewayProperties;
    private final ApiDecryptProperties apiDecryptProperties;

    public GlobalLogFilter(CustomGatewayProperties customGatewayProperties, ApiDecryptProperties apiDecryptProperties) {
        this.customGatewayProperties = customGatewayProperties;
        this.apiDecryptProperties = apiDecryptProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !Boolean.TRUE.equals(customGatewayProperties.getRequestLog());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        HttpServletRequest requestToUse = request;
        if (isJsonRequest(request) && !(request instanceof CachedBodyHttpServletRequest)) {
            requestToUse = new CachedBodyHttpServletRequest(request);
        }

        String url = requestToUse.getMethod() + " " + requestToUse.getRequestURI();
        logRequest(requestToUse, url);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(requestToUse, response);
        } finally {
            log.info("[PLUS]结束请求 => URL[{}],耗时:[{}]毫秒", url, System.currentTimeMillis() - startTime);
        }
    }

    private void logRequest(HttpServletRequest request, String url) {
        if (isJsonRequest(request)) {
            if (Boolean.TRUE.equals(apiDecryptProperties.getEnabled())
                && ObjectUtil.isNotNull(request.getHeader(apiDecryptProperties.getHeaderFlag()))) {
                log.info("[PLUS]开始请求 => URL[{}],参数类型[encrypt]", url);
                return;
            }
            String jsonParam = resolveBody(request);
            if (StringUtils.isNotBlank(jsonParam)) {
                jsonParam = removeSensitiveFields(jsonParam);
            }
            log.info("[PLUS]开始请求 => URL[{}],参数类型[json],参数:[{}]", url, jsonParam);
            return;
        }

        MultiValueMap<String, String> parameterMap = UriComponentsBuilder.newInstance()
            .query(request.getQueryString())
            .build()
            .getQueryParams();
        if (MapUtil.isNotEmpty(parameterMap)) {
            LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>(parameterMap);
            MapUtil.removeAny(map, SystemConstants.EXCLUDE_PROPERTIES);
            log.info("[PLUS]开始请求 => URL[{}],参数类型[param],参数:[{}]", url, JsonUtils.toJsonString(map));
        } else {
            log.info("[PLUS]开始请求 => URL[{}],无参数", url);
        }
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        return StringUtils.startsWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_JSON_VALUE);
    }

    private String resolveBody(HttpServletRequest request) {
        if (request instanceof CachedBodyHttpServletRequest cachedRequest) {
            return cachedRequest.getCachedBodyAsString();
        }
        return null;
    }

    private String removeSensitiveFields(String jsonParam) {
        try {
            ObjectMapper objectMapper = JsonUtils.getObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonParam);
            removeSensitiveFields(rootNode, SystemConstants.EXCLUDE_PROPERTIES);
            return rootNode.toString();
        } catch (Exception ex) {
            return jsonParam;
        }
    }

    private void removeSensitiveFields(JsonNode node, String[] excludeProperties) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Set<String> fieldsToRemove = new HashSet<>();
            objectNode.fieldNames().forEachRemaining(fieldName -> {
                if (ArrayUtil.contains(excludeProperties, fieldName)) {
                    fieldsToRemove.add(fieldName);
                }
            });
            fieldsToRemove.forEach(objectNode::remove);
            objectNode.elements().forEachRemaining(child -> removeSensitiveFields(child, excludeProperties));
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode child : arrayNode) {
                removeSensitiveFields(child, excludeProperties);
            }
        }
    }

}
