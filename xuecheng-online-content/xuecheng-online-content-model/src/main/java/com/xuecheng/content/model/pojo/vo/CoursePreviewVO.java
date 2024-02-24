package com.xuecheng.content.model.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Builder
@Data
public class CoursePreviewVO {

    //课程基本信息+课程营销信息
    CourseBaseInfoVO courseBase;

    //课程计划信息
    List<TeachPlanVO> teachplans;

    //师资信息暂时不加...

}
