package com.xuecheng.base.enumeration;

import lombok.Getter;

@Getter
public enum CourseAuditStatus {

    REVIEW_FAILED("202001"),

    UNCOMMITTED("202002"),

    COMMITTED("202003"),

    REVIEW_PASSED("202004");

    private final String value;

    CourseAuditStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

/*    public static CourseAuditStatus fromValue(String value) {
        for (CourseAuditStatus status : CourseAuditStatus.values()) {
            if (status.getValue().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status value: " + value);
    }*/

}
