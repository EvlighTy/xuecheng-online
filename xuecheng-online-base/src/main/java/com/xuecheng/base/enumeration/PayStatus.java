package com.xuecheng.base.enumeration;

//支付记录交易状态
public enum PayStatus {

    UNPAID("601001"), //未支付
    PAID("601002"), //已支付
    REFUNDED("601003"); //已退款

    private final String value;

    PayStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
