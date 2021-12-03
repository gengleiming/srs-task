package com.intellif.vesionbook.srstask.enums;


public enum StreamTaskStatusEnum {

    INIT(1, "创建中"),

    PROCESSING(2, "运行中"),

    CLOSED(3, "已关闭"),

    DISCONNECT(4, "未连接到服务器"),
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
