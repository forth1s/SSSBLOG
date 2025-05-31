package com.example.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 统一响应格式
 * @param <T> 响应数据类型
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;    // 状态码（建议使用 HTTP 状态码）
    private String message;  // 提示信息
    private T data;          // 响应数据

    // 成功响应（带数据）
    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    // 成功响应（无数据）
    // T 被指定为 Void 类型，表示不包含业务数据的响应结果
    public static Result<Void> success(String message) {
        return success(message, null);
    }

    // 错误响应（覆盖常见 HTTP 状态码）
    public static Result<Void> error(int code, String message) {
        return Result.<Void>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }

    // 常用错误快捷方法
    public static Result<Void> badRequest(String message) { return error(400, message); }
    public static Result<Void> unauthorized(String message) { return error(401, message); }
    public static Result<Void> forbidden(String message) { return error(403, message); }
    public static Result<Void> notFound(String message) { return error(404, message); }
    public static Result<Void> serverError(String message) { return error(500, message); }
}