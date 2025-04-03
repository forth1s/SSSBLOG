package com.example.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

public class JwtTokenUtil {
    private static final long EXPIRATION_TIME = 1000*60*60;
    private static final String SECRET_KEY = "askjhdsu@1.sd5163213hdfbasjdf";

    /**
     * 生成 JWT Token 的方法
     * @param username 用户名
     * @return 生成的 JWT Token
     */
    public static String generateToken(String username) {
        // 创建 HMAC256 算法实例，使用定义的密钥
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        // 创建 JWT 构建器
        JWTCreator.Builder builder = JWT.create();
        // 设置 JWT 的主题为用户名
        builder.withSubject(username);
        // 设置 JWT 的过期时间为当前时间加上定义的过期时长
        builder.withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME));
        // 使用算法对 JWT 进行签名并生成 Token
        return builder.sign(algorithm);
    }
}
