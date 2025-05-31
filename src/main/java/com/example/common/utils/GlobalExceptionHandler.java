package com.example.common.utils;

import com.example.entity.Result;
import com.example.common.exceptions.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ======================= 自定义异常处理 =======================

    // 业务异常（自定义状态码）
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.getCode()).body(Result.error(e.getCode(), e.getMessage()));
    }

    // 参数校验异常（400）
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Result<Void>> handleBadRequestException(BadRequestException e) {
        return ResponseEntity.badRequest().body(Result.badRequest(e.getMessage()));
    }

    // 安全异常（401）
    // UnauthorizedExceptions是业务逻辑产生的自定义未认证异常
    // AuthenticationException是spring security链中认证失败时自动产生的异常类型
    @ExceptionHandler({UnauthorizedException.class, AuthenticationException.class})
    public ResponseEntity<Result<Void>> handleSecurityException(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.unauthorized(e.getMessage()));
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<Result<Void>> handleForbiddenException(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.forbidden("权限不足：" + e.getMessage())); // 返回自定义响应格式
    }

    // 服务器内部错误（500）
    @ExceptionHandler(ServerException.class)
    public ResponseEntity<Result<Void>> handleServerException(ServerException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.serverError(e.getMessage()));
    }

    // 重写 404 错误处理（替代原有的 handleNotFoundException）
//    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        Result<Void> result = Result.notFound("资源不存在：" + ex.getRequestURL());
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    // 重写 HTTP 方法不支持异常（如 GET 请求访问 POST 接口）
//    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        String message = "不支持的 HTTP 方法：" + ex.getMethod() + "，支持的方法：" + ex.getSupportedHttpMethods();
        Result<Void> result = Result.badRequest(message);
        return new ResponseEntity<>(result, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // 其他基础异常处理可按需重写（如 HttpMediaTypeNotSupportedException）
}
