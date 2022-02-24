package com.intellif.vesionbook.srstask.enums;


public enum StreamTypeEnum {

    RTSP(1, "rtsp"),
    GB28181(2, "gb28181"),
    RTMP(3, "rtmp"),
    ;

    private final int code;

    private final String name;

    StreamTypeEnum(int code, String name) {
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
