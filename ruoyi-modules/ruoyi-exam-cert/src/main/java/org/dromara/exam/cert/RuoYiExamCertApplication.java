package org.dromara.exam.cert;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 证书服务
 *
 * @author ruoyi
 */
@EnableDubbo
@SpringBootApplication
public class RuoYiExamCertApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiExamCertApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  证书服务启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
