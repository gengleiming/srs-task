package com.intellif.vesionbook.srstask.enums;


public enum VideoRecorderTaskStatusEnum {

    INIT(0, "未开始"),
    RUNNING(1, "运行中"),
    FINISHED(2, "已结束"),
    ERROR(3, "任务异常"),
    ;

    private final int code;

    private final String name;

    VideoRecorderTaskStatusEnum(int code, String name) {
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
