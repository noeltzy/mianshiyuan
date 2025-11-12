package com.tzy.mianshiyuan.common;

public enum ErrorCode {
    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_FOUND(40400, "请求数据不存在"),
    NO_AUTH(40101, "未登录或无权限"),
    FORBIDDEN(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    FILE_UPLOAD_ERROR(50002, "文件上传失败"),
    FILE_NOT_FOUND(40401, "文件不存在"),
    FILE_SIZE_EXCEEDED(40001, "文件大小超限"),
    FILE_TYPE_NOT_SUPPORTED(40002, "不支持的文件类型");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}


