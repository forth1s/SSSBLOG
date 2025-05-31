package com.example.security.filters;

import com.example.common.enums.BusinessType;
import com.example.common.enums.CaptchaBusinessType;
import com.example.common.utils.RedisUtil;
import com.example.common.utils.Util;
import com.example.security.handlers.SecurityExceptionHandler;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CaptchaFilter extends OncePerRequestFilter {

    private final RedisUtil redisUtil;
    private final SecurityExceptionHandler exceptionHandler;

    public CaptchaFilter(
            RedisUtil redisUtil,
            SecurityExceptionHandler exceptionHandler
    ) {
        this.redisUtil = redisUtil;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws IOException, ServletException {
        if (shouldValidateCaptcha(request)) {
            try {
                String uuid = Util.extractUUid(request);
                String businessType = Util.extractBusinessType(request);
                String captchaRedisKey = getCaptchaRedisKey(businessType, uuid);

                validateCaptcha(request, captchaRedisKey);
                redisUtil.delete(captchaRedisKey);
            } catch (Exception e) {
                exceptionHandler.commence(
                        request,
                        response,
                        new BadCredentialsException(e.getMessage())
                );
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getCaptchaRedisKey(String businessType, String captchaId) {
        if (!StringUtils.hasLength(businessType) || !StringUtils.hasLength(captchaId)) {
            return null;
        }
        CaptchaBusinessType type = BusinessType.fromName(businessType, CaptchaBusinessType.class);
        return type.getPrefix() + captchaId;
    }

    private boolean shouldValidateCaptcha(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        return "POST".equals(method) && (
                uri.startsWith("/login") ||
                        uri.startsWith("/register") ||
                        uri.startsWith("/forgot-password") ||
                        uri.startsWith("/reset-password")
        );
    }

    private void validateCaptcha(HttpServletRequest request, String verifyCodeKey) {
        String inputCaptcha = request.getParameter("captcha");
        if (!StringUtils.hasLength(inputCaptcha)) {
            throw new BadCredentialsException("没有填写验证码");
        }

        String correctCaptcha = (String) redisUtil.get(verifyCodeKey);
        if (!StringUtils.hasLength(correctCaptcha)) {
            throw new BadCredentialsException("验证码失效，请重新获取");
        }

        if (!correctCaptcha.equalsIgnoreCase(inputCaptcha)) {
            throw new BadCredentialsException("验证码错误，请重新获取");
        }
    }
}
