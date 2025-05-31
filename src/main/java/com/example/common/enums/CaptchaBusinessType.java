package com.example.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public enum CaptchaBusinessType implements BusinessType {
    LOGIN("login", "登录", "LOGIN_", 2, TimeUnit.MINUTES),  // 登录验证码2分钟过期
    REGISTER("register", "注册", "REGISTER_", 5, TimeUnit.MINUTES),  // 注册验证码5分钟过期
    SEND_EMAIL("send-email","发送邮件", "SEND_EMAIL_", 5, TimeUnit.MINUTES),
    FORGOT_PASSWORD("forgot-password", "忘记密码", "FORGOT_PASSWORD_", 30, TimeUnit.MINUTES), // 忘记密码30分钟
    RESET_PASSWORD("reset-password", "重置密码", "RESET_PASSWORD_", 30, TimeUnit.MINUTES);

    private final String name;        // 业务名
    private final String description; // 业务描述
    private final String prefix;      // Redis键前缀
    private final long expireTime;    // 过期时间
    private final TimeUnit timeUnit;  // 时间单位
}
