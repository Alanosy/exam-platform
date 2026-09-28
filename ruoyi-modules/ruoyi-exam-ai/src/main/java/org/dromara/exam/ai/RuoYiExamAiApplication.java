package org.dromara.exam.ai;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * AI 服务（Java 网关层）
 * <p>
 * 作为对外门面，将其他 exam 模块的 AI 调用请求转发至 Python FastAPI Agent 服务，
 * 同步走 REST，异步批量走 RocketMQ。本身不承载大模型推理。
 *
 * @author ruoyi
 */
@EnableDubbo
@SpringBootApplication
public class RuoYiExamAiApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiExamAiApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  AI 服务启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
