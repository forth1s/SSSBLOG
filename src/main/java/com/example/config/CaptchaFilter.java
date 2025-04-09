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

    private static final String IMAGE_VERIFY_CODE_PREFIX = "VERIFY_CODE_";
    private static final String PASSWORD_RESET_BY_EMAIL = "PASSWORD_RESET_BY_EMAIL_";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisUtil redisUtil;

    public CaptchaFilter(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {
        // 处理所有需要校验验证码的请求
        if (shouldValidateCaptcha(request)) {
            HttpSession session = request.getSession();
            String sessionId = session.getId();
            String verifyCodeKey = getVerifyCodeKey(request, sessionId);
            try {
                validateCaptcha(request, verifyCodeKey);
                redisUtil.delete(verifyCodeKey);
                filterChain.doFilter(request, response);
                return;
            } catch (IllegalArgumentException e) {
                redisUtil.delete(verifyCodeKey);
                sendErrorResponse(response, e.getMessage());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getVerifyCodeKey(HttpServletRequest request, String sessionId) {
        String uri = request.getRequestURI();
        if ("/reset-password".equals(uri)) {
            return PASSWORD_RESET_BY_EMAIL + sessionId;
        }
        return IMAGE_VERIFY_CODE_PREFIX + sessionId;
    }

    private boolean shouldValidateCaptcha(HttpServletRequest request) {
        String[] validUris = {"/login", "/register", "/reset-password", "/sendMail"};
        String method = request.getMethod();
        String uri = request.getRequestURI();
        if (!"POST".equals(method)) {
            return false;
        }
        for (String validUri : validUris) {
            if (validUri.equals(uri)) {
                return true;
            }
        }
        return false;
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

        if (!correctCaptcha.equalsIgnoreCase(inputCaptcha)) {
            throw new IllegalArgumentException("验证码输入错误，请重新获取");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        Result result = new Result("error", message);
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        try (PrintWriter out = response.getWriter()) {
            out.write(objectMapper.writeValueAsString(result));
            out.flush();
        }
    }
}