package com.example.controller;

import com.example.entity.Result;
import com.example.utils.JwtTokenUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenRefreshController {
    /**
     * 若旧token暴露，攻击者反而会使用该接口不断获取新token认证
     * 因此，需使用https协议，防止token暴露
     * */
    @PostMapping("/refresh-token")
    public Result refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String oldToken = authHeader.replace("Bearer ", "");
                String newToken = JwtTokenUtil.refreshToken(oldToken);
                if (newToken != null) {
                    return new Result("success", "Bearer " + newToken);
                } else {
                    return new Result("error", "无法刷新 Token");
                }
            } else {
                return new Result("error", "无效的 Token 格式");
            }
        } catch (Exception e) {
            // 可以根据具体异常类型进行不同处理，这里统一返回通用错误信息
            return new Result("error", "刷新 Token 时发生未知错误: " + e.getMessage());
        }
    }
}
