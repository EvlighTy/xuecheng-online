package com.xuecheng.base.enumeration;

public enum PayChannel {
    ALIPAY("Alipay"),
    WECHATPAY("WeChatPay");

    private final String value;
    PayChannel(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
