package com.xuecheng.base.enumeration;

//业务订单类型
public enum OrderType {
    PURCHASE_COURSE("60201"), //购买课程
    COURSE_MATERIAL("60202"); //学习资料

    private final String value;
    OrderType(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
