package com.example.common.exception;

import lombok.Getter;

// 客户端请求错误异常
@Getter
public class BadRequestException extends RuntimeException {
    private final Integer code;

    public BadRequestException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
