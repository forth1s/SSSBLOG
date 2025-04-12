package com.example.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用接口返回结果
 * @param <T> 响应数据类型
 */
@Data
@SuperBuilder // 支持链式构建和部分字段初始化
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 状态码：200=成功，4xx=客户端错误，5xx=服务器错误 */
    private Integer code;

    /** 状态描述：成功或错误消息 */
    private String message;

    /** 响应数据：成功时携带数据，失败时为 null */
    private T data;

    // 在 Result 类中添加
    public String toJSONString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
    }
}