package com.intellif.vesionbook.srstask.enums;


public enum VideoRecorderTaskStatusEnum {

    INIT(1, "未开始"),
    RUNNING(2, "运行中"),
    FINISHED(3, "已结束"),
    EXPIRE(4, "任务超时"),
    ERROR(5, "任务异常"),
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
