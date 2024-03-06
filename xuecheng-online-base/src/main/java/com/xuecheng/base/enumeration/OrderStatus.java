package com.xuecheng.base.enumeration;

//订单交易类型状态
public enum OrderStatus {

    UNPAID("600001"), //未支付
    PAID("600002"), //已支付
    CLOSED("600003"), //已关闭
    REFUNDED("600004"), //已退款
    FINISHED("600005"); //已完成

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
