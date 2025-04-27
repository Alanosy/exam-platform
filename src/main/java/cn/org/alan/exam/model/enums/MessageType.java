package cn.org.alan.exam.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/18 21:46
 */
@Getter
@AllArgsConstructor
public enum MessageType {
    // 讨论
    DISCUSSION,
    // 作业
    TASK,
    ;
}
