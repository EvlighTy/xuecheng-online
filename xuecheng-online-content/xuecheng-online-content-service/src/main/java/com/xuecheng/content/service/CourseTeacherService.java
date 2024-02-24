package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.pojo.dto.AddCourseTeacherDTO;
import com.xuecheng.content.model.pojo.entity.CourseTeacher;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务类
 * </p>
 *
 * @author itcast
 * @since 2024-02-11
 */
public interface CourseTeacherService extends IService<CourseTeacher> {

    List<CourseTeacher> getList(Long id);

    CourseTeacher add(AddCourseTeacherDTO addCourseTeacherDTO);

    CourseTeacher edit(CourseTeacher courseTeacher);

    void delete(Long courseId, Long teacherId);
}
