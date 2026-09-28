package org.dromara.exam.mark;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 阅卷服务
 *
 * @author ruoyi
 */
@EnableDubbo
@SpringBootApplication
public class RuoYiExamMarkApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiExamMarkApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  阅卷服务启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
