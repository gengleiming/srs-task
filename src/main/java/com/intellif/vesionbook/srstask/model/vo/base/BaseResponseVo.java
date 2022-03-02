package com.intellif.vesionbook.srstask.model.vo.base;

import com.intellif.vesionbook.srstask.enums.ReturnCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponseVo<T> {

    /**
     * 返回码
     */
    private int respCode = 10000000;

    /**
     * 返回标志, success fail
     */
    private String respMark = "SUCCESS";

    /**
     * 返回说明信息
     */
    private String respMessage;

    /**
     * 返回数据
     */
    private T data;

    public static <T> BaseResponseVo<T> ok() {
        return new BaseResponseVo<>(ReturnCodeEnum.SUCCESS.getResultCode(),
                ReturnCodeEnum.SUCCESS.getName(), ReturnCodeEnum.SUCCESS.getMessage(),
                null);
    }

    public static <T> BaseResponseVo<T> ok(T data) {
        return new BaseResponseVo<>(ReturnCodeEnum.SUCCESS.getResultCode(),
                ReturnCodeEnum.SUCCESS.getName(), ReturnCodeEnum.SUCCESS.getMessage(),
                data);
    }

    public static <T> BaseResponseVo<T> ok(T data, String respMessage) {
        return new BaseResponseVo<>(ReturnCodeEnum.SUCCESS.getResultCode(),
                ReturnCodeEnum.SUCCESS.getName(), respMessage, data);
    }

    public static <T> BaseResponseVo<T> error(ReturnCodeEnum resultCodeEnum) {
        return error(resultCodeEnum.getResultCode(), resultCodeEnum.getName(),
                resultCodeEnum.getMessage());
    }

    public static <T> BaseResponseVo<T> error(ReturnCodeEnum resultCodeEnum,
                                              String errorMsg) {
        return error(resultCodeEnum.getResultCode(), resultCodeEnum.getName(),
                errorMsg);
    }

    public static <T> BaseResponseVo<T> error(int respCode, String respMark, String errorMessage) {
        return new BaseResponseVo<>(respCode,
                respMark, errorMessage, null);
    }

    public static <T> BaseResponseVo<T> invalidParam(String errorMessage) {
        return new BaseResponseVo<>(ReturnCodeEnum.PARAM_INVALID.getResultCode(),
                ReturnCodeEnum.PARAM_INVALID.getName(), errorMessage, null);
    }

    public static <T> BaseResponseVo<T> error(ReturnCodeEnum resultCodeEnum, T data) {
        return error(resultCodeEnum.getResultCode(), resultCodeEnum.getName(),
                resultCodeEnum.getMessage(), data);
    }

    public static <T> BaseResponseVo<T> error(int respCode, String respMark, String errorMessage, T data) {
        return new BaseResponseVo<>(respCode,
                respMark, errorMessage, data);
    }

    public boolean isSuccess() {
        return this.getRespCode() == ReturnCodeEnum.SUCCESS.getResultCode();
    }

}
