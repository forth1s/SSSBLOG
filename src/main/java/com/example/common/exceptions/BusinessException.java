package com.example.common.exceptions;

import lombok.Getter;

// 自定义业务异常（客户端错误，4xx）
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
