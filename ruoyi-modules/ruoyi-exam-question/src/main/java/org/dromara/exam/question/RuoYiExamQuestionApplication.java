package org.dromara.exam.question;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 题库服务
 *
 * @author ruoyi
 */
@EnableDubbo
@SpringBootApplication
public class RuoYiExamQuestionApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiExamQuestionApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  题库服务启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
