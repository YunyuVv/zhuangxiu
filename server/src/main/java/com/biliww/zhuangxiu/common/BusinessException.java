package com.biliww.zhuangxiu.common;

import lombok.Getter;

/**
 * 业务异常：携带错误码，由 GlobalExceptionHandler 统一转成 ApiResult。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
