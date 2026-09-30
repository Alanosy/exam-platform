package cn.org.alan.exam.utils.agent.impl;

import cn.hutool.json.JSONObject;
import cn.org.alan.exam.utils.agent.AIChat;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Dify实现AIChat
 * @author 赵浩森
 * @since 2025/4/17 23:39
 * @version 1.0
 */
@Service
@ConditionalOnProperty(name = "online-exam.chat-platform.type", havingValue = "dify")
public class DifyUtil implements AIChat {
    @Value("${dify.api-key}")
    private String apiKey;

    @Value("${dify.base-url}")
    private String baseUrl;

    @Override
    public String getChatResponse(String msg) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(baseUrl);

            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("query", msg);
            requestBody.put("response_mode", "blocking");
            requestBody.put("user", "user"); // 用户标识

            // 根据文档添加 inputs 字段
            JSONObject inputs = new JSONObject();
            inputs.put("text", "Your input text"); // 按实际需求调整内容
            requestBody.put("inputs", inputs);

            // 调试输出请求体
//            System.out.println("Request Body: " + requestBody);

            StringEntity entity = new StringEntity(requestBody.toString());
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseString = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) {
                    return new JSONObject(responseString).getStr("answer");
                } else {
                    throw new RuntimeException("HTTP " + statusCode + " Error: " + responseString);
                }
            }
        }
    }
}