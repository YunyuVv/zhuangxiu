package com.biliww.zhuangxiu.common;

import lombok.Getter;

/**
 * 统一错误码：0 成功；4xx 业务错误；5xx 系统错误。
 */
@Getter
public enum ErrorCode {

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),

    SYSTEM_ERROR(500, "系统错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
