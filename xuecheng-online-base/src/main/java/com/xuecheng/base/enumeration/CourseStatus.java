package com.xuecheng.base.enumeration;

import lombok.Getter;

@Getter
public enum CourseStatus {

    UNPUBLISHED("203001"),

    PUBLISHED("203002"),

    OFFLINE("203001");

    private final String value;

    CourseStatus(String value) {
        this.value=value;
    }

}
