package com.xuecheng.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.vo.XcChooseCourseVO;
import com.xuecheng.learning.model.vo.XcCourseTablesVO;

public interface ChooseCourseService extends IService<XcChooseCourse> {

    XcChooseCourseVO addChooseCourse(Long courseId);

    XcCourseTablesVO getLearnStatus(Long courseId);

    void updateChooseCourseAndSave2CourseTables(String chooserCourseId);
}
