package com.xuecheng.content.model.pojo.dto;

import com.xuecheng.base.model.PageParams;
import lombok.Data;

@Data
public class CourseQueryDTO extends PageParams {
    String courseName;
    String auditStatus;
    String publishStatus;
}
