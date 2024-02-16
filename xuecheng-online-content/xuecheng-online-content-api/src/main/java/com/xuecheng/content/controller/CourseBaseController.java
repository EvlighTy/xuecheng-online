package com.xuecheng.content.controller;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.result.PageResult;
import com.xuecheng.base.model.result.Result;
import com.xuecheng.content.model.pojo.dto.AddCourseDTO;
import com.xuecheng.content.model.pojo.dto.CourseQueryDTO;
import com.xuecheng.content.model.pojo.dto.EditCourseDTO;
import com.xuecheng.content.model.pojo.entity.CourseBase;
import com.xuecheng.content.model.pojo.vo.CourseBaseInfoVO;
import com.xuecheng.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("course")
public class CourseBaseController {

    @Autowired
    private CourseBaseService  courseBaseService;

/*    @PostMapping("/list")
    public Result<PageResult<CourseBase>> list(@RequestBody CourseQueryDTO courseQueryDTO){
        PageResult<CourseBase> pageResult = courseBaseService.getList(courseQueryDTO);
        return Result.success(pageResult);
    }*/

    @PostMapping("/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) CourseQueryDTO courseQueryDTO){
        log.info("分页查询课程");
        PageResult<CourseBase> pageResult = courseBaseService.getList(pageParams,courseQueryDTO);
        return pageResult;
    }

    @PostMapping
    public CourseBaseInfoVO add(@RequestBody @Validated AddCourseDTO addCourseDTO){
        log.info("添加课程");
        CourseBaseInfoVO courseBaseInfoVO = courseBaseService.add(addCourseDTO);
        return courseBaseInfoVO;
    }

    @GetMapping("/{id}")
    public CourseBaseInfoVO get(@PathVariable Long id){
        log.info("查询课程");
        CourseBaseInfoVO courseBaseInfoVO = courseBaseService.get(id);
        return courseBaseInfoVO;
    }

    @PutMapping
    public CourseBaseInfoVO edit(@RequestBody EditCourseDTO editCourseDTO){
        log.info("修改课程");
//        CourseBaseInfoVO courseBaseInfoVO = courseBaseService.edit(editCourseDTO);
        CourseBaseInfoVO courseBaseInfoVO = new CourseBaseInfoVO();
        courseBaseInfoVO.setId(editCourseDTO.getId());
        return courseBaseInfoVO;
    }
}
