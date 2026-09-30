package org.dromara.exam.practice;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 练习服务
 *
 * @author ruoyi
 */
@EnableDubbo
@SpringBootApplication
public class RuoYiExamPracticeApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiExamPracticeApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  练习服务启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
