package com.xuecheng.content.jobhandler;

import cn.hutool.core.bean.BeanUtil;
import com.xuecheng.content.model.pojo.entity.CoursePublish;
import com.xuecheng.content.model.pojo.feign.CourseIndex;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class CoursePublishJob extends MessageProcessAbstract {

    @Autowired
    private CoursePublishService coursePublishService;


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
        //阶段一(生成静态页面并上传到minio)
        executeStageOne(mqMessage);
        //阶段二(生成课程文档索引保存到ES)
        executeStageTwo(mqMessage);
        //阶段三(保存数据到Redis)
        executeStageThree(mqMessage);
        return true;
    }

    //阶段一
    private void executeStageOne(MqMessage mqMessage) {
        log.info("执行阶段一任务");
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(id);
        /*幂等性*/
        if(stageOne>0) return;
        //执行任务
        String courseId = mqMessage.getBusinessKey1();
        /*生成课程静态页面*/
        File courseHtml = coursePublishService.getCourseHtml(Long.valueOf(courseId));
        /*课程静态页面上传至minio*/
        coursePublishService.uploadHtmlToMinio(courseHtml, Long.valueOf(courseId));
        //更新信息
        mqMessageService.completedStageOne(id);
    }

    //阶段二
    private void executeStageTwo(MqMessage mqMessage){
        log.info("执行阶段二任务");
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(id);
        /*幂等性*/
        if(stageTwo>0) return;
        //执行任务
        CoursePublish coursePublish = coursePublishService.getById(mqMessage.getBusinessKey1());
        CourseIndex courseIndex = BeanUtil.copyProperties(coursePublish, CourseIndex.class);
        coursePublishService.addCourseIndex(courseIndex);
        //更新信息
        mqMessageService.completedStageTwo(id);
    }

    //阶段三
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
