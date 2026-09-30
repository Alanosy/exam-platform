package org.dromara.gateway.filter;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.SystemConstants;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.json.utils.JsonUtils;
import org.dromara.gateway.config.properties.ApiDecryptProperties;
import org.dromara.gateway.config.properties.CustomGatewayProperties;
import org.dromara.gateway.utils.WebFluxUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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
public class GlobalLogFilter implements GlobalFilter, Ordered {

    @Autowired
    private CustomGatewayProperties customGatewayProperties;
    @Autowired
    private ApiDecryptProperties apiDecryptProperties;

    private static final String START_TIME = "startTime";

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!customGatewayProperties.getRequestLog()) {
            return chain.filter(exchange);
        }
        ServerHttpRequest request = exchange.getRequest();
        String path = WebFluxUtils.getOriginalRequestUrl(exchange);
        String url = request.getMethod().name() + " " + path;

        // 打印请求参数
        if (WebFluxUtils.isJsonRequest(exchange)) {
            if (apiDecryptProperties.getEnabled()
                && ObjectUtil.isNotNull(request.getHeaders().getFirst(apiDecryptProperties.getHeaderFlag()))) {
                log.info("[PLUS]开始请求 => URL[{}],参数类型[encrypt]", url);
            } else {
                String jsonParam = WebFluxUtils.resolveBodyFromCacheRequest(exchange);
                if (StringUtils.isNotBlank(jsonParam)) {
                    ObjectMapper objectMapper = JsonUtils.getObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(jsonParam);
                    removeSensitiveFields(rootNode, SystemConstants.EXCLUDE_PROPERTIES);
                    jsonParam = rootNode.toString();
                }
                log.info("[PLUS]开始请求 => URL[{}],参数类型[json],参数:[{}]", url, jsonParam);
            }
        } else {
            MultiValueMap<String, String> parameterMap = request.getQueryParams();
            if (MapUtil.isNotEmpty(parameterMap)) {
                LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>(parameterMap);
                MapUtil.removeAny(map, SystemConstants.EXCLUDE_PROPERTIES);
                String parameters = JsonUtils.toJsonString(map);
                log.info("[PLUS]开始请求 => URL[{}],参数类型[param],参数:[{}]", url, parameters);
            } else {
                log.info("[PLUS]开始请求 => URL[{}],无参数", url);
            }
        }

        exchange.getAttributes().put(START_TIME, System.currentTimeMillis());
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Long startTime = exchange.getAttribute(START_TIME);
            if (startTime != null) {
                long executeTime = (System.currentTimeMillis() - startTime);
                log.info("[PLUS]结束请求 => URL[{}],耗时:[{}]毫秒", url, executeTime);
            }
        }));
    }

    private void removeSensitiveFields(JsonNode node, String[] excludeProperties) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            // 收集要删除的字段名（避免 ConcurrentModification）
            Set<String> fieldsToRemove = new HashSet<>();
            objectNode.fieldNames().forEachRemaining(fieldName -> {
                if (ArrayUtil.contains(excludeProperties, fieldName)) {
                    fieldsToRemove.add(fieldName);
                }
            });
            fieldsToRemove.forEach(objectNode::remove);
            // 递归处理子节点
            objectNode.elements().forEachRemaining(child -> removeSensitiveFields(child, excludeProperties));
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode child : arrayNode) {
                removeSensitiveFields(child, excludeProperties);
            }
        }
    }

    @Override
    public int getOrder() {
        // 日志处理器在负载均衡器之后执行 负载均衡器会导致线程切换 无法获取上下文内容
        // 如需在日志内操作线程上下文 例如获取登录用户数据等 可以打开下方注释代码
        // return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER - 1;
        return Ordered.LOWEST_PRECEDENCE;
    }

}
