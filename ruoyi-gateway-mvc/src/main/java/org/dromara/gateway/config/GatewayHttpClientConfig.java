package org.dromara.gateway.config;

import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.dromara.gateway.config.properties.GatewayHttpClientProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;

/**
 * 网关 HttpClient5 配置
 *
 * @author RuoYi
 */
@Configuration
@RequiredArgsConstructor
public class GatewayHttpClientConfig {

    private final GatewayHttpClientProperties properties;

    /**
     * Gateway MVC 使用 RestClient 转发请求，此处提供 HttpClient5 请求工厂以调整连接池参数
     */
    @Bean
    @RefreshScope
    public ClientHttpRequestFactory gatewayClientHttpRequestFactory(ClientHttpRequestFactorySettings settings) {
        return ClientHttpRequestFactoryBuilder.httpComponents()
            .withConnectionManagerCustomizer(connectionManager -> {
                connectionManager.setMaxConnTotal(properties.getMaxConnTotal());
                connectionManager.setMaxConnPerRoute(properties.getMaxConnPerRoute());
            })
            .withConnectionConfigCustomizer(connectionConfig -> {
                if (properties.getConnectionTimeToLive() != null) {
                    connectionConfig.setTimeToLive(TimeValue.of(properties.getConnectionTimeToLive()));
                }
                if (properties.getValidateAfterInactivity() != null) {
                    connectionConfig.setValidateAfterInactivity(TimeValue.of(properties.getValidateAfterInactivity()));
                }
            })
            .withDefaultRequestConfigCustomizer(requestConfig -> {
                if (properties.getConnectionRequestTimeout() != null) {
                    requestConfig.setConnectionRequestTimeout(Timeout.of(properties.getConnectionRequestTimeout()));
                }
                if (properties.getResponseTimeout() != null) {
                    requestConfig.setResponseTimeout(Timeout.of(properties.getResponseTimeout()));
                }
            })
            .build(settings);
    }

}
