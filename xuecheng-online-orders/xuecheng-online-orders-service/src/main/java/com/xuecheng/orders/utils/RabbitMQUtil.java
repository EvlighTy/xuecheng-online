package com.xuecheng.orders.utils;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.config.RabbitMQConfig;
import com.xuecheng.base.enumeration.OrderType;
import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class RabbitMQUtil {

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public CorrelationData getCorrelationData(Long messageId){
        CorrelationData correlationData = new CorrelationData(messageId.toString());
        correlationData.getFuture().addCallback(
                result->{
                    if (result!=null && result.isAck()){
                        log.info("消息发送到交换机成功[{}]",correlationData.getId());
                        //删除消息表中的记录
                        mqMessageService.completed(messageId);
                    }else {
                        log.error("消息发送到交换机失败[{}] | 原因:[{}]",correlationData.getId(),result.getReason());
                    }
                },
                ex->{
                    log.error("消息发送失败[{}] | 原因:[{}]",correlationData.getId(),ex.getMessage());
                }
        );
        return correlationData;
    }

    public void sendMessage(Object object,Long messageId){
        CorrelationData correlationData = getCorrelationData(messageId);
        Message message = MessageBuilder.withBody(JSON.toJSONString(object).getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
//                .setExpiration("5000")
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.PAY_NOTIFY_EXCHANGE_FANOUT, //交换机
                OrderType.PURCHASE_COURSE.getValue(), //routingKey
                message,
                correlationData);
    }

}
