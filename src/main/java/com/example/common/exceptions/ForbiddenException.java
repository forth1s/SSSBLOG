package com.example.common.exceptions;

import lombok.Getter;

// 安全相关异常（授权失败）
@Getter
public class ForbiddenException extends RuntimeException {
    private final Integer code;
    public ForbiddenException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
