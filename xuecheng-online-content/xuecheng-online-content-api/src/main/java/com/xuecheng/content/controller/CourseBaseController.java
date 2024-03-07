package com.xuecheng.content.controller;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.model.result.PageResult;
import com.xuecheng.base.model.result.Result;
import com.xuecheng.base.utils.SecurityUtil;
import com.xuecheng.content.model.pojo.dto.AddCourseDTO;
import com.xuecheng.content.model.pojo.dto.CourseQueryDTO;
import com.xuecheng.content.model.pojo.dto.EditCourseDTO;
import com.xuecheng.content.model.pojo.entity.CourseBase;
import com.xuecheng.content.model.pojo.vo.CourseBaseInfoVO;
import com.xuecheng.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 课程基本信息 前端控制器
 * </p>
 *
 * @author itcast
 */
@Slf4j
@RestController
@RequestMapping
public class CourseBaseController {

    @Autowired
    private CourseBaseService  courseBaseService;

/*    @PostMapping("/list")
    public Result<PageResult<CourseBase>> list(@RequestBody CourseQueryDTO courseQueryDTO){
        PageResult<CourseBase> pageResult = courseBaseService.getList(courseQueryDTO);
        return Result.success(pageResult);
    }*/

    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) CourseQueryDTO courseQueryDTO){
        log.info("分页查询课程");
        PageResult<CourseBase> pageResult = courseBaseService.getList(pageParams,courseQueryDTO);
        return pageResult;
    }

    @PreAuthorize("hasAnyAuthority('xc_teachmanager_course')")
    @PostMapping("/course")
    public CourseBaseInfoVO add(@RequestBody @Validated AddCourseDTO addCourseDTO){
        log.info("添加课程");
        CourseBaseInfoVO courseBaseInfoVO = courseBaseService.add(addCourseDTO);
        return courseBaseInfoVO;
    }


    @GetMapping("/course/{id}")
    public CourseBaseInfoVO get(@PathVariable Long id){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        log.info("查询课程");
        CourseBaseInfoVO courseBaseInfoVO = courseBaseService.get(id);
        return courseBaseInfoVO;
    }

    @PutMapping("/course")
    public CourseBaseInfoVO edit(@RequestBody EditCourseDTO editCourseDTO){
        log.info("修改课程");
        CourseBaseInfoVO courseBaseInfoVO = courseBaseService.edit(editCourseDTO);
        return courseBaseInfoVO;
    }

    @DeleteMapping("/course/{id}")
    public Result<String> delete(Long id){
       courseBaseService.delete(id);
       return Result.success("删除课程成功");
    }

    @GetMapping("/courseoffline/{courseId}")
    public RestResponse courseOffline(@PathVariable Long courseId){
        log.info("下架课程");
        return courseBaseService.courseOffline(courseId);
    }
}
