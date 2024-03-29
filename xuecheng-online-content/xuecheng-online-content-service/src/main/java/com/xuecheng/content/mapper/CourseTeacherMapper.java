package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.pojo.entity.CourseTeacher;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 课程-教师关系表 Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Mapper
public interface CourseTeacherMapper extends BaseMapper<CourseTeacher> {

}
