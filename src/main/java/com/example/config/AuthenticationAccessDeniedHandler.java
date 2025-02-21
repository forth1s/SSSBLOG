package com.example.config;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;

public class AuthenticationAccessDeniedHandler implements AccessDeniedHandler{
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.write("权限不足,请联系管理员!");
        out.flush();
        out.close();
    }
}
