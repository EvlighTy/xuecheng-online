package com.xuecheng.base.enumeration;

//学习资格
public enum LearnStatus {

    NORMAL("702001"), //正常
    ABNORMAL("702002"), //未选课或未支付
    EXPIRED("702003"); //已过期

    private final String value;

    LearnStatus(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
