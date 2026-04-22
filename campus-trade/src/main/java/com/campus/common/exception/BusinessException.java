package com.campus.common.exception;


/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message)             { super(message); this.code = 500; }
    public BusinessException(int code, String message)   { super(message); this.code = code; }

    public int getCode() { return code; }

    public static BusinessException of(String msg)       { return new BusinessException(msg); }
    public static BusinessException notFound(String msg) { return new BusinessException(404, msg); }
    public static BusinessException forbidden(String msg){ return new BusinessException(403, msg); }
    public static BusinessException badRequest(String msg){ return new BusinessException(400, msg); }
}
