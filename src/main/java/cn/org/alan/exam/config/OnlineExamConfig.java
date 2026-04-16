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

    @Data
    public static class AutoScoringConfig {
        /**
         * 是否启用AI自动阅卷功能
         */
        private Boolean enabled = false;
    }
}
