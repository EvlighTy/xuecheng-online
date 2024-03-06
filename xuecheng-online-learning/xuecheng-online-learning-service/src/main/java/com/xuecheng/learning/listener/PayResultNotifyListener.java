package com.xuecheng.learning.listener;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.config.RabbitMQConfig;
import com.xuecheng.learning.service.ChooseCourseService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PayResultNotifyListener {

    @Autowired
    private ChooseCourseService chooseCourseService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(RabbitMQConfig.PAY_NOTIFY_QUEUE),
            exchange = @Exchange(RabbitMQConfig.PAY_NOTIFY_EXCHANGE_FANOUT),
            key = "60201"
    ))
    public void DLQueueListener(Message message){
        MqMessage mqMessage = JSON.parseObject(new String(message.getBody()), MqMessage.class);
        log.info("接收到"+RabbitMQConfig.PAY_NOTIFY_QUEUE+"的消息:[" + mqMessage + "]");
        chooseCourseService.updateChooseCourseAndSave2CourseTables(mqMessage.getBusinessKey1());
    }

}
