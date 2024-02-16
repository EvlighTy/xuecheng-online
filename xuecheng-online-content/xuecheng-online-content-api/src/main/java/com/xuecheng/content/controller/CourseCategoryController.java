package com.xuecheng.content.controller;

import com.xuecheng.content.model.pojo.entity.CourseCategory;
import com.xuecheng.content.model.pojo.vo.CourseCategoryTreeVO;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 课程分类 前端控制器
 * </p>
 *
 * @author itcast
 */
@Slf4j
@RestController
@RequestMapping("/course-category")
public class CourseCategoryController {

    @Autowired
    private CourseCategoryService  courseCategoryService;

    @GetMapping("/tree-nodes")
    public List<CourseCategoryTreeVO> courseCategoryTreeVO(){
        List<CourseCategoryTreeVO> courseCategoryTreeVOS = courseCategoryService.courseCategoryTreeVO("1");
        return courseCategoryTreeVOS;
    }

}
