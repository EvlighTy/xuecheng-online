package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.result.PageResult;
import com.xuecheng.content.model.pojo.dto.AddCourseDTO;
import com.xuecheng.content.model.pojo.dto.CourseQueryDTO;
import com.xuecheng.content.model.pojo.dto.EditCourseDTO;
import com.xuecheng.content.model.pojo.entity.CourseBase;
import com.xuecheng.content.model.pojo.vo.CourseBaseInfoVO;

/**
 * <p>
 * 课程基本信息 服务类
 * </p>
 *
 * @author itcast
 * @since 2024-02-11
 */
public interface CourseBaseService extends IService<CourseBase> {

    PageResult<CourseBase> getList(PageParams pageParams,CourseQueryDTO courseQueryDTO);

    CourseBaseInfoVO add(AddCourseDTO addCourseDTO);

    CourseBaseInfoVO get(Long id);

    CourseBaseInfoVO edit(EditCourseDTO editCourseDTO);

    void delete(Long id);
}
