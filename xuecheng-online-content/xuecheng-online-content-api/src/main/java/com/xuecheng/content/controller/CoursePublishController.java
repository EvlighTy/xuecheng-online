package com.xuecheng.content.controller;

import com.xuecheng.content.model.pojo.vo.CoursePreviewVO;
import com.xuecheng.content.service.CoursePublishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * <p>
 * 课程发布 前端控制器
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Controller
@RequestMapping
public class CoursePublishController {

    @Autowired
    private CoursePublishService  coursePublishService;

    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){
        log.info("预览课程");
        CoursePreviewVO coursePreviewVO = coursePublishService.preview(courseId);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model",coursePreviewVO);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        log.info("提交审核课程");
        coursePublishService.commitAudit(courseId);
    }

    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursePublish(@PathVariable("courseId") Long courseId){
        log.info("发布课程");
        coursePublishService.coursePublish(courseId);
    }

}
