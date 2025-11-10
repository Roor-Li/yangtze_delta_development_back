package com.opengms.yddback.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    FAIL(400, "操作失败"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    // 业务错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // 自定义业务错误 1xxx
    PARAM_ERROR(1001, "参数错误"),
    DATA_NOT_FOUND(1002, "数据不存在"),
    DATA_ALREADY_EXISTS(1003, "数据已存在"),
    OPERATION_FAILED(1004, "操作失败"),

    // 数据库相关 2xxx
    DATABASE_ERROR(2001, "数据库操作失败"),
    DATA_INTEGRITY_ERROR(2002, "数据完整性错误"),

    // 文件相关 3xxx
    FILE_UPLOAD_ERROR(3001, "文件上传失败"),
    FILE_NOT_FOUND(3002, "文件不存在"),
    FILE_SIZE_EXCEEDED(3003, "文件大小超出限制");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;
}