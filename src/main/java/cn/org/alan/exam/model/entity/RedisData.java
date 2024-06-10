package cn.org.alan.exam.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Alan
 * @Version
 * @Date 2024/6/9 11:21 PM
 */
@Data
public class RedisData {
    private LocalDateTime expireTime;
    private Object data;
}
