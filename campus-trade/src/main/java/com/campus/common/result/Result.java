package com.campus.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * 统一 API 响应体
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    private final int    code;
    private final String message;
    private final T      data;

    private Result(int code, String message, T data) {
        this.code    = code;
        this.message = message;
        this.data    = data;
    }

    public static <T> Result<T> success()               { return new Result<>(200, "success", null); }
    public static <T> Result<T> success(T data)         { return new Result<>(200, "success", data); }
    public static <T> Result<T> success(String msg, T data) { return new Result<>(200, msg, data); }
    public static Result<Void> success(String message) { return new Result<>(200, message, null); }
    public static <T> Result<T> fail(String message)    { return new Result<>(500, message, null); }
    public static <T> Result<T> fail(int code, String message) { return new Result<>(code, message, null); }

    public static <T> Result<T> unauthorized()          { return new Result<>(401, "请先登录", null); }
    public static <T> Result<T> forbidden()             { return new Result<>(403, "权限不足", null); }
    public static <T> Result<T> notFound(String msg)    { return new Result<>(404, msg, null); }
}
