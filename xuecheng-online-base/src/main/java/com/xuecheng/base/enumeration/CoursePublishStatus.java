package com.xuecheng.base.enumeration;

//课程发布状态
public enum CoursePublishStatus {

    UNPUBLISHED("203001"),

    PUBLISHED("203002"),

    OFFLINE("203001");

    private final String value;

    CoursePublishStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
