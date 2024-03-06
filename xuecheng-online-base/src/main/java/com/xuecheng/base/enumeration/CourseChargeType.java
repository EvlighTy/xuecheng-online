package com.xuecheng.base.enumeration;

//课程收费情况
public enum CourseChargeType {

    FREE("201000"),
    CHARGE("201001");

    private final String value;

    CourseChargeType(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
