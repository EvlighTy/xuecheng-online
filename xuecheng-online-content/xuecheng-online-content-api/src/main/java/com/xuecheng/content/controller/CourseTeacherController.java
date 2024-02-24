package com.xuecheng.content.controller;

import com.xuecheng.base.model.result.Result;
import com.xuecheng.content.model.pojo.dto.AddCourseTeacherDTO;
import com.xuecheng.content.model.pojo.dto.AddTeachPlanDTO;
import com.xuecheng.content.model.pojo.entity.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.util.List;

/**
 * <p>
 * 课程-教师关系表 前端控制器
 * </p>
 *
 * @author itcast
 */
@Slf4j
@RestController
@RequestMapping("/courseTeacher")
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService  courseTeacherService;

    @GetMapping("/list/{id}")
    public List<CourseTeacher> getList(@PathVariable Long id){
        log.info("分页查询教师");
        List<CourseTeacher> courseTeachers = courseTeacherService.getList(id);
        return courseTeachers;
    }

    @PostMapping
    public CourseTeacher add(@RequestBody AddCourseTeacherDTO addCourseTeacherDTO){
        log.info("添加教师");
        CourseTeacher courseTeacher = courseTeacherService.add(addCourseTeacherDTO);
        return courseTeacher;
    }

    @PutMapping
    public CourseTeacher edit(@RequestBody CourseTeacher courseTeacher){
        log.info("修改教师");
        courseTeacherService.edit(courseTeacher);
        return courseTeacher;
    }

    @DeleteMapping("/course/{courseId}/{teacherId}")
    public Result<String> delete(@PathVariable Long courseId,@PathVariable Long teacherId){
        log.info("删除教师");
        courseTeacherService.delete(courseId,teacherId);
        return Result.success("删除成功",null);
    }

}
