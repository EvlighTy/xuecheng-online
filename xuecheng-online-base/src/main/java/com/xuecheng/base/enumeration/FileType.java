package com.xuecheng.base.enumeration;

//资源类型
public enum FileType {
    IMAGE("001001"),

    VIDEO("001002"),

    OTHERS("001003");

    private final String value;

    FileType(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
