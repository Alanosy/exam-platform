package cn.org.alan.exam.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2025/3/22 10:50
 */
@ServerEndpoint("/websocket")
@Component
@Slf4j
public class WebsocketHandler {

    private static final ConcurrentHashMap<Integer, Session> SESSION_MAP = new ConcurrentHashMap<>();


    @Autowired
    public void setInstance() {

    }

    @OnOpen
    public void onOpen(Session session) {
        // 获取用户id
        Integer userId = getUserIdBySession(session);
        if (Objects.nonNull(SESSION_MAP.get(userId)) && SESSION_MAP.get(userId).isOpen()) {
            // 如果map中有该用户的session信息，就不再重复加入连接
            return;
        }

        // 加入连接 放入map管理
        SESSION_MAP.put(userId, session);

        log.info("[websocket消息]：用户 {} 加入连接，当前连接总数：{}", userId, SESSION_MAP.size());
    }


    @OnClose
    public void onClose(Session session) {
        Integer userId = getUserIdBySession(session);
        Session existSession = SESSION_MAP.get(userId);
        if (Objects.isNull(existSession)) {
            // 获取不到直接不移除
            return;
        }
        // 断开连接从map移除
        SESSION_MAP.remove(userId);


        log.info("[websocket消息]：用户 {} 断开连接", userId);
    }

    @OnError
    public void onError(Throwable throwable) {
        log.error("WebSocket error: {}", throwable.getMessage());
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        // 反序列化字符串信息获取消息信息
        // Message msg = JSON.parseObject(message, Message.class);

        // 发送给所有用户
        sendAllMessage(message);
        log.info("[websocket消息]：收到消息 {}", message);

    }

    /**
     * 广播所有人信息
     *
     * @param message 信息
     */
    private void sendAllMessage(String message) {
        SESSION_MAP.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    synchronized (WebsocketHandler.class) {
                        session.getBasicRemote().sendText(message);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    /**
     * 从session连接路径中获取userId
     *
     * @param session session
     * @return 用户id
     */
    private Integer getUserIdBySession(Session session) {
        String[] arr = session.getRequestURI().getQuery().split("=");
        return Integer.parseInt(arr[arr.length - 1]);
    }
}
