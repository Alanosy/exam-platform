package cn.org.alan.exam.utils;

import cn.org.alan.exam.service.IAutoScoringService;
import cn.org.alan.exam.utils.agent.AIChat;
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

    @Autowired
    private AIChat aiChat;

    @Test
    public void testAutoGrading() throws Exception {
        autoScoringService.autoScoringExam(116, 164);
        // 等待一段时间，确保异步任务完成（可根据实际情况调整）
        System.out.println("异步方法执行！");
        Thread.sleep(100000);
    }

    @Test
    public void testAIChat() throws Exception {
        System.out.println(aiChat.getChatResponse("[\n" +
                "  {\n" +
                "    \"题目ID\": \"ST-01\",\n" +
                "    \"题目内容\": \"解释哈希表冲突解决机制\",\n" +
                "    \"题目总分\": 25,\n" +
                "    \"标准答案\": [\"开放定址法\", \"链地址法\", \"再哈希法\"],\n" +
                "    \"待评分答案\": \"主要使用链表连接冲突元素，有时也会在数组内寻找空位\"\n" +
                "  },\n" +
                " {\n" +
                "    \"题目ID\": \"ST-02\",\n" +
                "    \"题目内容\": \"描述二叉树先序遍历的递归实现过程\",\n" +
                "    \"题目总分\": 30,\n" +
                "    \"标准答案\": [\n" +
                "      \"访问根结点\",\n" +
                "      \"递归遍历左子树\",\n" +
                "      \"递归遍历右子树\",\n" +
                "      \"终止条件：空结点直接返回\"\n" +
                "    ],\n" +
                "    \"待评分答案\": \"先访问根节点，然后遍历右子树，最后遍历左子树。当遇到空节点时停止递归\"\n" +
                "  }\n" +
                "]"));
    }
}