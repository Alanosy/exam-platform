package cn.org.alan.exam.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 在线考试系统自定义配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "online-exam")
public class OnlineExamConfig {

    /**
     * AI自动阅卷配置
     */
    private AutoScoringConfig autoScoring;

    /**
     * Agent 微服务集成配置
     */
    private AgentConfig agent;

    @Data
    public static class AutoScoringConfig {
        /**
         * 是否启用AI自动阅卷功能
         */
        private Boolean enabled = false;
    }

    @Data
    public static class AgentConfig {
        /**
         * 是否启用 Agent 微服务批改（启用后优先委托 agent 服务，失败回退 Coze）
         */
        private Boolean scoringEnabled = false;

        /**
         * Agent 微服务基础地址
         */
        private String baseUrl = "http://127.0.0.1:8081";
    }
}
