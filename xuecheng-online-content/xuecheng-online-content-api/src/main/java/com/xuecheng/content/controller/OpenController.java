package com.xuecheng.content.controller;

import com.xuecheng.content.model.pojo.vo.CoursePreviewVO;
import com.xuecheng.content.service.CoursePublishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping
public class OpenController {

    @Autowired
    private CoursePublishService coursePublishService;

    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewVO getPreviewInfo(@PathVariable("courseId") Long courseId) {
        log.info("获取课程预览信息");
        return coursePublishService.preview(courseId);
    }

}
