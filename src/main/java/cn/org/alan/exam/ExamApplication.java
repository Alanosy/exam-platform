package cn.org.alan.exam;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800 * 2 )
public class ExamApplication{

    public static void main(String[] args) {
        SpringApplication.run(ExamApplication.class, args);
    }
}