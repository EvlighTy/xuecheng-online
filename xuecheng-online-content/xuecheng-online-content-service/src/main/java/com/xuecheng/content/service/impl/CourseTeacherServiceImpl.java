package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.constant.ExMsgConstant;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.pojo.dto.AddCourseTeacherDTO;
import com.xuecheng.content.model.pojo.entity.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {

    //分页查询课程教师
    @Override
    public List<CourseTeacher> getList(Long id) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<CourseTeacher>()
                .eq(CourseTeacher::getCourseId, id);
        List<CourseTeacher> courseTeachers = list(queryWrapper);
        return courseTeachers;
    }

    //新增教师
    @Override
    public CourseTeacher add(AddCourseTeacherDTO addCourseTeacherDTO) {
        //业务逻辑校验

        CourseTeacher courseTeacher = BeanUtil.copyProperties(addCourseTeacherDTO, CourseTeacher.class);
        boolean save = save(courseTeacher);
        if(!save) throw new CustomException(ExMsgConstant.INSERT_FAILED);
        return getById(courseTeacher.getId());
    }

    //修改教师
    @Override
    public CourseTeacher edit(CourseTeacher courseTeacher) {
        //业务逻辑校验

        boolean update = updateById(courseTeacher);
        if(!update) throw new CustomException(ExMsgConstant.UPDATE_FAILED);
        return courseTeacher;
    }

    //删除教师
    @Override
    public void delete(Long courseId, Long teacherId) {
        //业务逻辑校验

        boolean remove = removeById(teacherId);
        if(!remove) throw new CustomException(ExMsgConstant.DELETE_FAILED);
    }

}
