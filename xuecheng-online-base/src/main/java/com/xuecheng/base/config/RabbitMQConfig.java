package com.xuecheng.base.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            if(message.getMessageProperties().getReceivedDelay()<=0){
                log.error("消息发送到队列失败->响应码:[{}],失败原因:[{}],交换机:[{}],路由键:[{}],消息:[{}]",
                        replyCode,replyText,exchange,routingKey,message);
            }
        });
    }

    public static final String PAY_NOTIFY_EXCHANGE_FANOUT = "pay_notify.fanout"; //交换机
    public static final String PAY_NOTIFY_QUEUE = "pay_notify.queue"; //队列

}
