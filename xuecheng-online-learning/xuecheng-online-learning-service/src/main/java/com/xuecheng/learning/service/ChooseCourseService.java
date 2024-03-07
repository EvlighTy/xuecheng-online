package com.xuecheng.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.result.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableDTO;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.model.vo.XcChooseCourseVO;
import com.xuecheng.learning.model.vo.XcCourseTablesVO;

public interface ChooseCourseService extends IService<XcChooseCourse> {

    XcChooseCourseVO addChooseCourse(String userId,Long courseId);

    XcCourseTablesVO getLearnStatus(String userId, Long courseId);

    void updateChooseCourseAndSave2CourseTables(String chooserCourseId);

    PageResult<XcCourseTables> getMyCourseTable(MyCourseTableDTO myCourseTableDTO);
}
