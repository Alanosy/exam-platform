package cn.org.alan.exam.utils;

import cn.org.alan.exam.service.IAutoScoringService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author 赵浩森
 * @since 2025/4/12 18:57
 * @version 1.0
 */
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class) // 启用 Spring 测试支持
@SpringBootTest // 加载 Spring Boot 应用上下文
public class IAutoScoringServiceTest {

    @Autowired
    private IAutoScoringService autoScoringService;

    @Test
    public void testAutoGrading() throws Exception {
        autoScoringService.autoScoringExam(116, 164);
        // 等待一段时间，确保异步任务完成（可根据实际情况调整）
        System.out.println("异步方法执行！");
        Thread.sleep(100000);
    }
}