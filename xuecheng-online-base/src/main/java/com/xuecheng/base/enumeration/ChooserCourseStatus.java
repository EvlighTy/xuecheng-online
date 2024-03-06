package com.xuecheng.base.enumeration;

//选课状态
public enum ChooserCourseStatus {

    SUCCESS("701001"), //选课成功
    UNPAID("701002"); //待支付

    private final String value;
    ChooserCourseStatus(String value){
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
