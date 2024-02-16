package com.xuecheng.content.model.pojo.vo;

import com.xuecheng.content.model.pojo.entity.CourseCategory;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class CourseCategoryTreeVO extends CourseCategory {
    List<CourseCategoryTreeVO> childrenTreeNodes=new ArrayList<>();
}
