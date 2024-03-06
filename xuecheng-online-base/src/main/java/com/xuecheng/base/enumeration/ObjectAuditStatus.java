package com.xuecheng.base.enumeration;

//对象的审核状态
public enum ObjectAuditStatus {

    REVIEW_FAILED("002001"),

    UNREVIEWED("002002"),

    REVIEW_PASSED("002003");

    private final String value;

    ObjectAuditStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
