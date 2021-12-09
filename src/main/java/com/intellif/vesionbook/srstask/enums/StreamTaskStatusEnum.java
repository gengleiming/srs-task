package com.intellif.vesionbook.srstask.enums;


public enum StreamTaskStatusEnum {

    PROCESSING(1, "运行中"),
    CLOSED(2, "已关闭"),
    ;

    private final int code;

    private final String name;

    StreamTaskStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
