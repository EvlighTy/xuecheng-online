package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.CourseTableService;
import org.springframework.stereotype.Service;

@Service
public class CourseTableServiceImpl extends ServiceImpl<XcCourseTablesMapper, XcCourseTables> implements CourseTableService {
}
