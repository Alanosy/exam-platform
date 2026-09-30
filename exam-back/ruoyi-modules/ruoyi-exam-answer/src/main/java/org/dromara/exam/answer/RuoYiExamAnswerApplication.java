package org.dromara.exam.answer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 答题服务
 *
 * @author ruoyi
 */
@EnableDubbo
// 超时未交卷的答卷由定时任务兜底自动交卷
@EnableScheduling
@SpringBootApplication
public class RuoYiExamAnswerApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiExamAnswerApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  答题服务启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
