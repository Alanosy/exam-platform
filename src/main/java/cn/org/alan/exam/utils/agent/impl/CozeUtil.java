package cn.org.alan.exam.utils.agent.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.org.alan.exam.utils.agent.AIChat;
import com.coze.openapi.client.chat.*;
import com.coze.openapi.client.chat.model.ChatPoll;
import com.coze.openapi.client.connversations.message.model.Message;
import com.coze.openapi.service.auth.TokenAuth;
import com.coze.openapi.service.service.CozeAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * coze 平台实现 AIChat
 *
 * @author 赵浩森
 * @since 2025/4/12 21:23
 * @version 1.0
 */
@Service
@ConditionalOnProperty(name = "online-exam.chat-platform.type", havingValue = "coze")
public class CozeUtil implements AIChat {
    @Value("${coze.api.token}")
    private String token;

    @Value("${coze.api.bot}")
    private String botID;

    @Value("${coze.api.base-url}")
    private String baseUrl;

    private String uid = "USER_ID";

    public String getChatResponse(String msg) throws Exception {
        TokenAuth authCli = new TokenAuth(token);

        // Init the Coze client through the access_token.
        CozeAPI coze =
                new CozeAPI.Builder()
                        .baseURL(baseUrl)
                        .auth(authCli)
                        .readTimeout(10000)
                        .build();

        CreateChatReq req =
                CreateChatReq.builder()
                        .botID(botID)
                        .userID(uid)
                        .messages(Collections.singletonList(Message.buildUserQuestionText(msg)))
                        .build();

        ChatPoll chat = coze.chat().createAndPoll(req);

        // 获取type为answer对应的content
        final Optional<String> answer = chat.getMessages().stream()
                .filter(message -> message.getType().getValue().equals("answer"))
                .map(Message::getContent)
                .findFirst();

        return answer.orElseThrow(() -> new RuntimeException("No answer found"));
    }
}