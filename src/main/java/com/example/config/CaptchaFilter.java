package com.example.config;

import com.example.entity.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.annotation.Nonnull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

public class CaptchaFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain)
            throws ServletException, IOException {
        // 只对登录请求进行验证码校验
        if ("/login".equals(request.getRequestURI()) && "POST".equals(request.getMethod())) {
            String inputCaptcha = request.getParameter("captcha");
            String correctCaptcha = (String) request.getSession().getAttribute("CAPTCHA");

            if (inputCaptcha == null ||!inputCaptcha.equalsIgnoreCase(correctCaptcha)) {
                // 构造统一响应格式
                Result result = new Result("error", "验证码错误!");
                // 设置响应状态码为 400
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                // 设置响应内容类型为 JSON 并指定字符编码
                response.setContentType("application/json;charset=UTF-8");
                // 将 Result 对象转换为 JSON 字符串
                String jsonResponse = objectMapper.writeValueAsString(result);
                // 获取 PrintWriter 并将 JSON 响应写入响应体
                try (PrintWriter out = response.getWriter()) {
                    out.print(jsonResponse);
                    out.flush();
                }
                return;
            }
        }
        // 继续执行后续过滤器
        filterChain.doFilter(request, response);
    }
}