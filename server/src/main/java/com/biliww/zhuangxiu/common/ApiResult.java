package com.biliww.zhuangxiu.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 统一返回体：code=0 成功；4xx 业务错误；5xx 系统错误（按研发评审结论）。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {

    private int code;
    private String message;
    private T data;

    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> r = new ApiResult<>();
        r.setCode(0);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static ApiResult<Void> success() {
        return success(null);
    }

    public static ApiResult<Void> error(int code, String message) {
        ApiResult<Void> r = new ApiResult<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
