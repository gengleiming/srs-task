package com.intellif.vesionbook.srstask.enums;

public enum ReturnCodeEnum {

    SUCCESS(10000000, "success", "成功"),

    UNKNOWN_ERROR(80000000, "unknownError", "未知异常"),

    PARAM_ERROR(70000000, "parameterError", "参数错误"),

    PARAM_INVALID(60000000, "parameterInvalid", "请求参数非法"),

    JSON_ERROR(50000000, "jsonError", "JSON转换失败"),

    DB_ERROR(40000000, "dbError", "数据库异常"),

    NETWORK_ERROR(30000000, "networkError", "网络异常"),

    GET_DATA_ERROR(90000001, "data error", "获取接口返回数据错误"),

    ERROR_STREAM_TASK_REPEAT(90000002, "stream task repeat error", "发现视频流任务重复"),
    ERROR_STREAM_TASK_TYPE_NOT_SUPPORT(90000003, "stream task type not support", "暂不支持的分发格式"),
    ERROR_STREAM_TASK_DATABASE_NOT_FOUND(90000004, "stream task database not found", "数据库未发现该流任务，请稍后再试"),
    ERROR_STREAM_TASK_CACHE_NOT_FOUND(90000005, "stream task cache not found", "缓存未发现该流任务，请稍后再试"),
    ERROR_STREAM_TASK_FAILED(90000006, "stream task failed", "流任务执行失败"),
    ERROR_STREAM_TASK_NOT_ALIVE(90000007, "stream task not alive", "流任务不在存活状态"),
    ERROR_STREAM_TASK_NONE(90000008, "stream task none", "流任务不存在"),
    ERROR_STREAM_TASK_MAX_LIMIT(90000009, "stream task max limit", "流任务数量超出限制"),
    ERROR_GB_CHANNEL(90000010, "stream task gb channel error", "gb281818通道异常"),
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

    ReturnCodeEnum(int resultCode, String name, String message) {
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

    public static ReturnCodeEnum get(int resultCode) {
        if (resultCode > 0) {
            return null;
        }
        for (ReturnCodeEnum c : ReturnCodeEnum
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
