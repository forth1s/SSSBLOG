package com.example.config;

import com.example.common.exception.ForbiddenException;
import com.example.common.exception.BadRequestException;
import com.example.common.exception.ServerException;
import com.example.common.utils.RedisUtil;
import com.example.common.utils.ResponseUtil;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CaptchaFilter extends OncePerRequestFilter {

    private static final String IMAGE_VERIFY_CODE_PREFIX = "VERIFY_CODE_";
    private static final String PASSWORD_RESET_BY_EMAIL = "PASSWORD_RESET_BY_EMAIL_";

    private final RedisUtil redisUtil;

    public CaptchaFilter(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (shouldValidateCaptcha(request)) {
                HttpSession session = request.getSession();
                String sessionId = session.getId();
                String verifyCodeKey = getVerifyCodeKey(request, sessionId);
                validateCaptcha(request, verifyCodeKey);
                redisUtil.delete(verifyCodeKey);
            }
            filterChain.doFilter(request, response);
        } catch (BadRequestException e) {
            ResponseUtil.sendErrorResponse(response, e.getCode(), e.getMessage());
        } catch (Exception e){
            ResponseUtil.sendServerErrorResponse(response, e.getMessage());
        }
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
        boolean isUriValid = false;
        for (String validUri : validUris) {
            if (validUri.equals(uri)) {
                isUriValid = true;
                break;
            }
        }
        if (!isUriValid) {
            return false;
        }
        if (!"POST".equals(method)) {
            throw new BadRequestException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,"访问接口的请求方式错误");
        }
        return true;
    }

    private void validateCaptcha(HttpServletRequest request, String verifyCodeKey) {
        String inputCaptcha = request.getParameter("captcha");
        if (!StringUtils.hasLength(inputCaptcha)) {
            throw new BadRequestException(HttpServletResponse.SC_BAD_REQUEST, "验证码不能为空");
        }

        String correctCaptcha = (String) redisUtil.get(verifyCodeKey);
        if (!StringUtils.hasLength(correctCaptcha)) {
            throw new BadRequestException(HttpServletResponse.SC_BAD_REQUEST, "验证码失效，请重新获取");
        }

        if (!correctCaptcha.equalsIgnoreCase(inputCaptcha)) {
            throw new BadRequestException(HttpServletResponse.SC_BAD_REQUEST, "验证码错误，请重新获取");
        }
    }
}