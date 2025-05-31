package com.example.common.utils;

import com.example.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

public class Util {
    private Util() {

    }
    public static User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * @return 业务类型
     */
    public static String extractBusinessType(HttpServletRequest request) {
        // 优先从自定义请求头获取 businessType（前端路由拦截器自动设置）
        String bizType = request.getHeader("X-Business-Type");
        if (bizType != null && !bizType.isEmpty()) {
            return bizType;
        }
        return null;
    }

    public static String extractUUid(HttpServletRequest request) {
        // 优先从自定义请求头获取 businessType（前端路由拦截器自动设置）
        String uuid = request.getHeader("X-UUID");
        if (uuid != null && !uuid.isEmpty()) {
            return uuid;
        }
        return null;
    }
}
