package com.xuecheng.content.jobhandler;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoursePublishJob extends MessageProcessAbstract {

    @XxlJob("coursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        process(shardIndex,shardTotal,"course_publish",10,60);
    }

    @Override
    public boolean execute(MqMessage mqMessage) {
        String courseId = mqMessage.getBusinessKey1();
        //第一阶段(生成静态页面)
        executeStageOne(mqMessage);
        executeStageTwo(mqMessage);
        executeStageThree(mqMessage);
        return true;
    }

    private void executeStageOne(MqMessage mqMessage) {
        log.info("执行阶段一任务");
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(id);
        /*幂等性*/
        if(stageOne>0) return;
        //执行任务 todo

        //更新信息
        mqMessageService.completedStageOne(id);
    }

    private void executeStageTwo(MqMessage mqMessage){
        log.info("执行阶段二任务");
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(id);
        /*幂等性*/
        if(stageTwo>0) return;
        //执行任务 todo

        //更新信息
        mqMessageService.completedStageTwo(id);
    }

    private void executeStageThree(MqMessage mqMessage){
        log.info("执行阶段三任务");
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageThree = mqMessageService.getStageThree(id);
        /*幂等性*/
        if(stageThree>0) return;
        //执行任务 todo

        //更新信息
        mqMessageService.completedStageThree(id);
    }
}
