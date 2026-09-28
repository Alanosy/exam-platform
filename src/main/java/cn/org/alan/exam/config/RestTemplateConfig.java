package cn.org.alan.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类
 *
 * @Author Alan
 * @Version
 * @Date 2024/9/28
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 配置RestTemplate Bean
     *
     * @return
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 设置连接超时时间（毫秒）
        factory.setConnectTimeout(5000);
        // 设置读取超时时间（毫秒）
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }
}
