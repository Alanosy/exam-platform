package cn.org.alan.exam.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 消息体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

    /**
     * 发送者
     */
    private String sender;

    /**
     * 消息内容
     */
    private String content;

}
