package com.xuecheng.base.enumeration;

public enum MessageType {
    COURSE_PUBLISH("course_publish"),
    PAY_RESULT_NOTIFY("pay_result_notify");

    private final String value;
    MessageType(String value){
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
