package com.example.config;

import com.example.common.exception.BadRequestException;
import com.example.common.utils.RedisUtil;
import com.example.common.utils.ResponseUtil;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CaptchaFilter extends OncePerRequestFilter {

    private static final String VERIFY_CODE_PREFIX = "VERIFY_CODE_";
    private static final String PASSWORD_RESET_PREFIX = "PASSWORD_RESET_BY_EMAIL_";

    private final RedisUtil redisUtil;

    public CaptchaFilter(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws IOException {
        try {
            if (shouldValidateCaptcha(request)) {
                String captchaId = request.getParameter("uuid"); // 从参数获取UUID
                String verifyCodeKey = getVerifyCodeKey(request, captchaId);
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

    /**
     * 根据业务类型获取Redis键（支持不同业务前缀）
     */
    private String getVerifyCodeKey(HttpServletRequest request, String captchaId) {
        String uri = request.getRequestURI();
        if ("/reset-password".equals(uri)) {
            return PASSWORD_RESET_PREFIX + captchaId;
        }
        return VERIFY_CODE_PREFIX + captchaId;
    }


    /**
     * 判断是否需要验证验证码（支持POST请求和指定URI）
     */
    private boolean shouldValidateCaptcha(HttpServletRequest request) {
        String[] validUris = {"/login", "/register", "/reset-password", "/sendMail"};
        String method = request.getMethod();
        String uri = request.getRequestURI();
        // 检查URI是否在需要验证的列表中
        boolean isUriValid = false;
        for (String validUri : validUris) {
            if (validUri.equals(uri)) {
                isUriValid = true;
                break;
            }
        }
        // 必须为POST请求且URI有效
        return isUriValid && "POST".equals(method);
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