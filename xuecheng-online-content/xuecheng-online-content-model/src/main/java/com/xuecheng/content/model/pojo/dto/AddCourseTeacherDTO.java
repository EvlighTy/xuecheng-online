package com.xuecheng.content.model.pojo.dto;

import lombok.Data;

@Data
public class AddCourseTeacherDTO {
    private Long courseId;
    private String teacherName;
    private String position;
    private String introduction;
}
