package com.example.config;

import com.example.common.exception.*;
import com.example.common.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;

@ControllerAdvice
@RestControllerAdvice
@Configuration
@ComponentScan(basePackages = "com.example.config")
public class GlobalExceptionHandler {

    // ========== 业务异常处理 ==========
    @ExceptionHandler(BusinessException.class)
    public void businessExceptionHandler(HttpServletResponse response, BusinessException e) throws IOException {
        ResponseUtil.sendErrorResponse(response, e.getCode(), e.getMessage());
    }

    // ========== 参数校验异常处理 ==========
    @ExceptionHandler(BadRequestException.class)
    public void validationExceptionHandler(HttpServletResponse response, BadRequestException e) throws IOException {
        ResponseUtil.sendClientErrorResponse(response, e.getMessage()); // 400错误
    }

    // ========== 404资源不存在 ==========
    @ExceptionHandler(NoHandlerFoundException.class)
    public void notFoundExceptionHandler(HttpServletResponse response) throws IOException {
        ResponseUtil.sendNotFoundResponse(response, "资源不存在");
    }

    // ========== 安全相关异常（认证/授权失败）==========
    @ExceptionHandler({UnauthorizedException.class, ForbiddenException.class})
    public void securityExceptionHandler(HttpServletResponse response, Exception e) throws IOException {
        if (e instanceof UnauthorizedException) {
            ResponseUtil.sendUnauthorizedResponse(response, e.getMessage()); // 401
        } else {
            ResponseUtil.sendForbiddenResponse(response, e.getMessage()); // 403
        }
    }

    // ========== 服务器内部错误 ==========
    @ExceptionHandler(ServerException.class)
    public void globalExceptionHandler(HttpServletResponse response, ServerException e) throws IOException {
        ResponseUtil.sendServerErrorResponse(response, e.getMessage());
//        e.printStackTrace(); // 生产环境建议使用日志框架记录
    }
}