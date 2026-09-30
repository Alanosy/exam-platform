package org.dromara.exam.paper;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 试卷服务
 *
 * @author ruoyi
 */
@EnableDubbo
@SpringBootApplication
public class RuoYiExamPaperApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiExamPaperApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  试卷服务启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
