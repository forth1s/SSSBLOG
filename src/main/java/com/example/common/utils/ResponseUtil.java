package com.example.common.utils;

import com.example.entity.Result;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class ResponseUtil {

    private ResponseUtil() {
        // 工具类构造方法私有化
    }

    /**
     * 发送成功响应（无数据）
     * @param response HTTP响应对象
     * @param message 响应消息
     */
    public static void sendSuccessResponse(HttpServletResponse response, String message) throws IOException {
        sendSuccessResponse(response, HttpServletResponse.SC_OK, message, null);
    }

    /**
     * 发送成功响应（带数据）
     * @param response HTTP响应对象
     * @param message 响应消息
     * @param data 响应数据
     */
    public static void sendSuccessResponse(HttpServletResponse response, String message, Object data) throws IOException {
        sendSuccessResponse(response, HttpServletResponse.SC_OK, message, data);
    }

    /**
     * 发送成功响应（自定义状态码）
     * @param response HTTP响应对象
     * @param status HTTP状态码（推荐使用200-299范围）
     * @param message 响应消息
     * @param data 响应数据（无数据时传null）
     */
    public static void sendSuccessResponse(HttpServletResponse response, int status, String message, Object data) throws IOException {
        Result<Object> result = Result.<Object>builder()
                .code(status)
                .message(message)
                .data(data)
                .build();
        writeResponse(response, status, result);
    }

    /**
     * 发送客户端错误响应（400 Bad Request）
     * @param response HTTP响应对象
     * @param message 错误消息
     */
    public static void sendClientErrorResponse(HttpServletResponse response, String message) throws IOException {
        sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, message);
    }

    /**
     * 发送未认证响应（401 Unauthorized）
     * @param response HTTP响应对象
     * @param message 错误消息
     */
    public static void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    /**
     * 发送权限不足响应（403 Forbidden）
     * @param response HTTP响应对象
     * @param message 错误消息
     */
    public static void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, message);
    }

    /**
     * 请求资源不存在（404 Not Found）
     * @param response HTTP响应对象
     * @param message 错误消息
     */
    public static void sendNotFoundResponse(HttpServletResponse response, String message) throws IOException {
        sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, message);
    }

    /**
     * 发送服务器内部错误响应（500 Internal Server Error）
     * @param response HTTP响应对象
     * @param message 错误消息
     */
    public static void sendServerErrorResponse(HttpServletResponse response, String message) throws IOException {
        sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
    }

    /**
     * 发送自定义错误响应
     * @param response HTTP响应对象
     * @param status HTTP状态码（推荐使用400-599范围）
     * @param message 错误消息
     */
    public static void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        Result<Void> result = Result.<Void>builder()
                .code(status)
                .message(message)
                .data(null)
                .build();
        writeResponse(response, status, result);
    }

    private static void writeResponse(HttpServletResponse response, int status, Result<?> result) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=utf-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(result.toJSONString()); // 使用Result自带的序列化方法
            out.flush();
        }
    }
}