package com.xuecheng.base.enumeration;

//选课类型
public enum ChooserCourseType {

    FREE("700001"),
    CHARGE("700002");

    private final String value;

    ChooserCourseType(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
