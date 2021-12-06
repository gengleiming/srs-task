package com.intellif.vesionbook.srstask.enums;


public enum StreamOutputTypeEnum {

    RTMP(1, "rtmp"),

    HTTP_HLV(2, "http-hlv"),

    HLS(4, "hls"),

    WEB_RTC(8, "WebRTC"),
    ;

    private final int code;

    private final String name;

    StreamOutputTypeEnum(int code, String name) {
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
