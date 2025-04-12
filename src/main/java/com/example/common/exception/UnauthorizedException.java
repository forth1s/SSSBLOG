package com.example.common.exception;

import lombok.Getter;

// 安全相关异常（认证失败）
@Getter
public class UnauthorizedException extends RuntimeException {
    private final Integer code;
    public UnauthorizedException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
