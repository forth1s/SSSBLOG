//package com.example.config;
//
//import com.example.common.exception.BusinessException;
//import com.example.common.exception.ForbiddenException;
//import com.example.common.exception.UnauthorizedException;
//import com.example.common.utils.ResponseUtil;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.validation.BindException;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.servlet.NoHandlerFoundException;
//
//import java.io.IOException;
//import java.util.stream.Collectors;
//
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//    // ========== 业务异常处理 ==========
//    @ExceptionHandler(BusinessException.class)
//    public void businessExceptionHandler(HttpServletResponse response, BusinessException e) throws IOException {
//        ResponseUtil.sendErrorResponse(response, e.getCode(), e.getMessage());
//    }
//
//    // ========== 参数校验异常处理 ==========
//    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
//    public void validationExceptionHandler(HttpServletResponse response, Exception e) throws IOException {
//        String message = e instanceof MethodArgumentNotValidException ?
//                ((MethodArgumentNotValidException) e).getBindingResult().getAllErrors().stream()
//                        .map(error -> ((FieldError) error).getField() + ":" + error.getDefaultMessage())
//                        .collect(Collectors.joining("; ")) :
//                e.getMessage();
//        ResponseUtil.sendClientErrorResponse(response, message); // 400错误
//    }
//
//    // ========== 404资源不存在 ==========
//    @ExceptionHandler(NoHandlerFoundException.class)
//    public void notFoundExceptionHandler(HttpServletResponse response) throws IOException {
//        ResponseUtil.sendNotFoundResponse(response, "资源不存在");
//    }
//
//    // ========== 安全相关异常（认证/授权失败）==========
//    @ExceptionHandler({UnauthorizedException.class, ForbiddenException.class})
//    public void securityExceptionHandler(HttpServletResponse response, Exception e) throws IOException {
//        if (e instanceof UnauthorizedException) {
//            ResponseUtil.sendUnauthorizedResponse(response, "未认证，请先认证"); // 401
//        } else {
//            ResponseUtil.sendForbiddenResponse(response, "权限不足"); // 403
//        }
//    }
//
//    // ========== 服务器内部错误 ==========
//    @ExceptionHandler(Exception.class)
//    public void globalExceptionHandler(HttpServletResponse response, Exception e) throws IOException {
//        ResponseUtil.sendServerErrorResponse(response, "服务器内部错误，请联系管理员");
////        e.printStackTrace(); // 生产环境建议使用日志框架记录
//    }
//}