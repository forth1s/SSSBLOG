package com.example.common.exceptions;

import lombok.Getter;

// 服务器内部错误
@Getter
public class ServerException extends RuntimeException {
    private final Integer code;
    public ServerException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
