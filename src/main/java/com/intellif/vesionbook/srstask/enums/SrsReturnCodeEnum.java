package com.intellif.vesionbook.srstask.enums;

public enum SrsReturnCodeEnum {

    ERROR_GB_SERVER_NOT_START(6000, "gb28181 server not start", "gb28181服务没有启用"),
    ERROR_GB_SIP_SESSION_OR_CHANNEL_EXISTS(6001, "session or channel exists", "sip会话或媒体通道已存在"),
    ERROR_GB_SIP_SESSION_OR_CHANNEL_NOT_EXISTS(6002, "session or channel not exists", "sip会话或媒体通道不存在"),
    ERROR_GB_RTP_PORT_LIMIT(6003, "rtp port max limit", "rtp端口已分配完"),
    ERROR_GB_RTP_PORT_INVALID(6004, "rtp port max limit", "无效的端口分配方式"),
    ERROR_PARAM_EMPTY(6005, "param empty", "参数不能为空"),
    ERROR_API_NOT_SUPPORT(6006, "api not support", "不支持的api"),
    ERROR_SIP_SERVER_NOT_START(6007, "sip server not start", "sip服务没有启用"),
    ERROR_SIP_INVITE_FAILED(6008, "sip invite failed", "sip invite 失败"),
    ERROR_SIP_BYE_FAILED(6009, "sip bye failed", "sip bye 失败"),
    ERROR_SIP_INVITE_SUCCESS_BEFORE(6010, "sip invite success before", "invite 已调用成功"),
    ERROR_CREATE_CHANNEL_RTMP_FAILED(6011, "sip invite success before", "创建媒体通道rtmp复合器失败"),
    ERROR_SIP_DEVICE_NOT_ONLINE(6012, "sip device not online", "sip设备通道不在线"),
    ERROR_SIP_DEVICE_NOT_EXISTS(6013, "sip device not exists", "sip设备通道不存在"),

    ;

    private final int resultCode;

    /***
     * 状态码定义
     */
    private final String name;

    /***
     * 状态码详细描述
     */
    private final String message;

    SrsReturnCodeEnum(int resultCode, String name, String message) {
        this.resultCode = resultCode;
        this.name = name;
        this.message = message;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public static SrsReturnCodeEnum get(int resultCode) {
        if (resultCode > 0) {
            return null;
        }
        for (SrsReturnCodeEnum c : SrsReturnCodeEnum
            .values()) {
            if (c.getResultCode() == resultCode) {
                return c;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return message;
    }
}
