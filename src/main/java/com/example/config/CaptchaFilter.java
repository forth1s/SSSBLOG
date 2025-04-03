package com.example.config;

import com.example.entity.Result;
import com.example.utils.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

public class CaptchaFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisUtil redisUtil;

    public CaptchaFilter(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {
        // 处理登录和注册请求
        if (("/login".equals(request.getRequestURI()) || "/register".equals(request.getRequestURI()))
                && "POST".equals(request.getMethod())) {
            HttpSession session = request.getSession();
            String sessionId = session.getId();
            String verifyCodeKey = "VERIFY_CODE_" + sessionId;
            try {
                validateCaptcha(request, verifyCodeKey);
                // 验证通过，删除验证码并继续执行后续过滤器
                redisUtil.delete(verifyCodeKey);
                filterChain.doFilter(request, response);
                return;
            } catch (IllegalArgumentException e) {
                // 验证失败，返回错误响应
                redisUtil.delete(verifyCodeKey);
                Result result = new Result("error", "验证码验证出错！");
                response.setContentType("application/json;charset=utf-8");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                try (PrintWriter out = response.getWriter()) {
                    out.write(objectMapper.writeValueAsString(result));
                    out.flush();
                }
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void validateCaptcha(HttpServletRequest request, String verifyCodeKey) {
        String inputCaptcha = request.getParameter("captcha");
        if (!StringUtils.hasLength(inputCaptcha)) {
            throw new IllegalArgumentException("验证码不能为空");
        }

        String correctCaptcha = (String) redisUtil.get(verifyCodeKey);
        if (!StringUtils.hasLength(correctCaptcha)) {
            throw new IllegalArgumentException("验证码已失效，请重新获取");
        }

        if (!correctCaptcha.equalsIgnoreCase(inputCaptcha.trim())) {
            throw new IllegalArgumentException("验证码输入错误，请重新获取");
        }
    }
}