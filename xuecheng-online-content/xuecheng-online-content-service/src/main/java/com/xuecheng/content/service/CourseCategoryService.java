package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.pojo.entity.CourseCategory;
import com.xuecheng.content.model.pojo.vo.CourseCategoryTreeVO;

import java.util.List;

/**
 * <p>
 * 课程分类 服务类
 * </p>
 *
 * @author itcast
 * @since 2024-02-11
 */
public interface CourseCategoryService extends IService<CourseCategory> {

    List<CourseCategoryTreeVO> courseCategoryTreeVO(String id);

}
