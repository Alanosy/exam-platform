package org.dromara.exam.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.exam.ai.config.AiAgentProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Python Agent 的 HTTP 客户端
 *
 * <p>Java 侧与模型之间只隔这一层。所有对 agent 的调用都从这里出去，
 * 好处是超时、重试、寻址、错误码翻译只写一遍。
 *
 * <p><b>铁律：不把异常抛给业务侧。</b>AI 是旁路能力，agent 挂了要能让业务照常跑，
 * 所以所有失败都翻译成 {@code success=false} 的结果对象，由调用方决定怎么降级。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAgentClient {

    private final AiAgentProperties properties;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<DiscoveryClient> discoveryClientProvider;

    private volatile RestTemplate restTemplate;

    /**
     * agent 统一返回体
     *
     * <p>与 agent 侧 {@code R<T>} 对齐：{code, msg, data}。
     * code 200 才算成功，其余都当失败处理。
     */
    @Data
    public static class AgentResp {

        /** 业务码，200 为成功 */
        private int code;

        /** 提示信息 */
        private String msg;

        /** 业务数据 */
        private JsonNode data;
    }

    private RestTemplate restTemplate() {
        if (restTemplate == null) {
            synchronized (this) {
                if (restTemplate == null) {
                    // 直接用 Factory 设超时：RestTemplateBuilder 的 Duration 版已标记移除
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(properties.getConnectTimeout());
                    factory.setReadTimeout(properties.getReadTimeout());
                    restTemplate = new RestTemplate(factory);
                }
            }
        }
        return restTemplate;
    }

    /**
     * 解析 agent 基地址
     *
     * <p>配了 url 就直连；否则走 Nacos 服务发现取第一个健康实例。
     * 服务发现拿不到时回退到 url 的默认值，避免整条链路直接瘫掉。
     */
    public String resolveBaseUrl() {
        String url = properties.getUrl();
        if (StringUtils.isNotBlank(url)) {
            return StringUtils.trimToEmpty(url).replaceAll("/+$", "");
        }
        DiscoveryClient discoveryClient = discoveryClientProvider.getIfAvailable();
        if (discoveryClient != null) {
            try {
                List<ServiceInstance> instances = discoveryClient.getInstances(properties.getServiceName());
                if (instances != null && !instances.isEmpty()) {
                    ServiceInstance instance = instances.get(0);
                    return instance.getUri().toString().replaceAll("/+$", "");
                }
            } catch (Exception e) {
                log.warn("从 Nacos 获取 {} 实例失败，回退直连配置: {}", properties.getServiceName(), e.getMessage());
            }
        }
        return "http://127.0.0.1:9221";
    }

    /**
     * POST 调用 agent
     *
     * @param path 接口路径，如 /skill/mark_score/run
     * @param body 请求体，会序列化成 JSON
     * @return agent 返回体，网络异常或超时时 code 为 500
     */
    public AgentResp post(String path, Object body) {
        String url = resolveBaseUrl() + properties.getApiPrefix() + path;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String payload;
        try {
            payload = objectMapper.writeValueAsString(body == null ? Map.of() : body);
        } catch (Exception e) {
            log.warn("AI 请求序列化失败 path={} {}", path, e.getMessage());
            return failed(500, "请求序列化失败: " + e.getMessage());
        }

        int maxAttempt = Math.max(1, properties.getRetryCount() + 1);
        for (int i = 1; i <= maxAttempt; i++) {
            long start = System.currentTimeMillis();
            try {
                ResponseEntity<String> resp = restTemplate().postForEntity(
                    url, new HttpEntity<>(payload, headers), String.class);
                long cost = System.currentTimeMillis() - start;
                String raw = resp.getBody();
                if (StringUtils.isBlank(raw)) {
                    log.warn("AI 调用返回空 path={} cost={}ms", path, cost);
                    return failed(500, "agent 返回为空");
                }
                JsonNode node = objectMapper.readTree(raw);
                AgentResp agentResp = new AgentResp();
                agentResp.setCode(node.path("code").asInt(500));
                agentResp.setMsg(node.path("msg").asText(""));
                agentResp.setData(node.path("data"));
                if (agentResp.getCode() != 200) {
                    log.warn("AI 调用业务失败 path={} code={} msg={} cost={}ms", path, agentResp.getCode(), agentResp.getMsg(), cost);
                } else if (cost > 3000) {
                    // 慢调用单独留痕，方便定位是模型慢还是 agent 卡住
                    log.info("AI 慢调用 path={} cost={}ms", path, cost);
                }
                return agentResp;
            } catch (ResourceAccessException e) {
                // 超时 / 连不上，重试
                log.warn("AI 调用超时或不可达 path={} 第{}次 {}", path, i, e.getMessage());
                if (i == maxAttempt) {
                    return failed(500, "AI 服务不可达: " + e.getMessage());
                }
            } catch (Exception e) {
                log.warn("AI 调用异常 path={} {}", path, e.getMessage());
                return failed(500, "AI 调用异常: " + e.getMessage());
            }
        }
        return failed(500, "AI 调用失败");
    }

    /**
     * GET 调用 agent
     *
     * @param path 接口路径
     * @return agent 返回体
     */
    public AgentResp get(String path) {
        String url = resolveBaseUrl() + properties.getApiPrefix() + path;
        try {
            ResponseEntity<String> resp = restTemplate().getForEntity(url, String.class);
            String raw = resp.getBody();
            if (StringUtils.isBlank(raw)) {
                return failed(500, "agent 返回为空");
            }
            JsonNode node = objectMapper.readTree(raw);
            AgentResp agentResp = new AgentResp();
            agentResp.setCode(node.path("code").asInt(500));
            agentResp.setMsg(node.path("msg").asText(""));
            agentResp.setData(node.path("data"));
            return agentResp;
        } catch (Exception e) {
            log.warn("AI 查询异常 path={} {}", path, e.getMessage());
            return failed(500, "AI 服务不可达: " + e.getMessage());
        }
    }

    /**
     * 取 agent 的健康状态
     *
     * @return true 表示 agent 活着
     */
    public boolean ping() {
        // 日志里必须带上最终 URL：agent 挂了最常见的原因是「连错地址」而不是「代码写错」
        String url = resolveBaseUrl() + properties.getApiPrefix().replace("/api/ai", "") + "/health";
        try {
            ResponseEntity<String> resp = restTemplate().getForEntity(url, String.class);
            String body = resp.getBody();
            boolean up = StringUtils.isNotBlank(body) && body.contains("UP");
            if (!up) {
                log.warn("AI 健康检查返回异常 url={} body={}", url, StringUtils.abbreviate(body, 120));
            }
            return up;
        } catch (ResourceAccessException e) {
            // Connection refused / 超时都走这里：绝大多数情况是 agent 进程没在监听这个地址
            log.warn("AI 健康检查失败：agent 未启动或地址不通 url={} err={}", url, e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("AI 健康检查异常 url={} err={}", url, e.getMessage());
            return false;
        }
    }

    private AgentResp failed(int code, String msg) {
        AgentResp resp = new AgentResp();
        resp.setCode(code);
        resp.setMsg(msg);
        return resp;
    }
}
