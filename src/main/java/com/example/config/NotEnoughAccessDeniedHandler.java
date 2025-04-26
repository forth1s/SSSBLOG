package com.example.config;

import com.example.common.exception.ForbiddenException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class NotEnoughAccessDeniedHandler implements AccessDeniedHandler{
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) {
        throw  new ForbiddenException(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
//        ResponseUtil.sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "权限不足,请联系管理员!");
    }
}
