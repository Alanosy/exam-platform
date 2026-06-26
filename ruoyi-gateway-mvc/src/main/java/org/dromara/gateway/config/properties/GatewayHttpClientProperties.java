package org.dromara.gateway.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 网关 HttpClient5 配置
 *
 * @author RuoYi
 */
@Data
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "spring.cloud.gateway.httpclient")
public class GatewayHttpClientProperties {

    /**
     * 连接池最大连接数
     */
    private Integer maxConnTotal = 1000;

    /**
     * 单个路由最大连接数
     */
    private Integer maxConnPerRoute = 500;

    /**
     * 从连接池获取连接的等待超时时间
     */
    private Duration connectionRequestTimeout = Duration.ofSeconds(3);

    /**
     * 等待下游响应数据的超时时间，SSE 场景通常不设置
     */
    private Duration responseTimeout;

    /**
     * 连接最大存活时间
     */
    private Duration connectionTimeToLive;

    /**
     * 空闲连接可用性校验间隔
     */
    private Duration validateAfterInactivity = Duration.ofSeconds(30);

}
