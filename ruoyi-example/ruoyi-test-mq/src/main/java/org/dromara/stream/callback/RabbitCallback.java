package org.dromara.stream.callback;

import lombok.extern.slf4j.Slf4j;
import org.dromara.stream.config.RabbitConfig;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Rabbit回调
 * @author JC
 */

@Slf4j
@Component
public class RabbitCallback implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    private static final int MAX_RETRY_COUNT = 3;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info("消息发送成功: {}", correlationData);
        } else {
            log.error("消息发送失败: {}, 原因: {}", correlationData, cause);
            handleFailedMessage(correlationData);
        }
    }

    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        log.error("消息返回: ReplyCode: {}, ReplyText: {}, Exchange: {}, RoutingKey: {}, Message: {}",
            returnedMessage.getReplyCode(),
            returnedMessage.getReplyText(),
            returnedMessage.getExchange(),
            returnedMessage.getRoutingKey(),
            returnedMessage.getMessage());
        retrySendMessage(returnedMessage);
    }

    private void handleFailedMessage(CorrelationData correlationData) {
        int retryCount = getRetryCount(correlationData);
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            log.info("正在重试发送消息: {}, 当前重试次数: {}", correlationData, retryCount);
            retrySend(correlationData);
        } else {
            log.error("消息发送失败超过最大重试次数: {}", correlationData);
        }
    }

    private int getRetryCount(CorrelationData correlationData) {
        // 这里可以实现获取重试次数的逻辑，比如从数据库或缓存中获取
        // 为了简单起见，这里返回0
        return 0;
    }

    private void retrySend(CorrelationData correlationData) {
        String messageContent = correlationData.getId();
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY, messageContent, correlationData);
    }

    private void retrySendMessage(ReturnedMessage returnedMessage) {
        log.info("正在重试发送返回的消息: {}", returnedMessage.getMessage());
        rabbitTemplate.convertAndSend(returnedMessage.getExchange(), returnedMessage.getRoutingKey(), returnedMessage.getMessage());
    }
}
