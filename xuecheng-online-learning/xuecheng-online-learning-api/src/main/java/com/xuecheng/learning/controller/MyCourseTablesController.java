package com.xuecheng.learning.controller;

import com.xuecheng.base.model.result.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.model.vo.XcChooseCourseVO;
import com.xuecheng.learning.model.vo.XcCourseTablesVO;
import com.xuecheng.learning.service.ChooseCourseService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MyCourseTablesController {

    @Autowired
    private ChooseCourseService chooseCourseService;

    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseVO addChooseCourse(@PathVariable("courseId") Long courseId) {
        log.info("用户选课");
        return chooseCourseService.addChooseCourse(courseId);
    }

    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesVO getLearnStatus(@PathVariable("courseId") Long courseId) {
        log.info("用户查询课程学习资格");
        return chooseCourseService.getLearnStatus(courseId);
    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<XcCourseTables> mycoursetable(MyCourseTableParams params) {

        return null;
    }

}
