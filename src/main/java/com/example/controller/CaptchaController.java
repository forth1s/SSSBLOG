package com.example.controller;

import com.example.common.utils.Util;
import com.example.entity.Result;
import com.example.service.CaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    /**
     * 生成验证码接口
     */
    @GetMapping("/captcha")
    public Result<?> getCode(
            HttpServletRequest request
    ) {
        String businessType = Util.extractBusinessType(request);
        String uuid = Util.extractUUid(request);
        return Result.success(
                "获取验证码成功",
                captchaService.generateCaptcha(uuid, businessType)
        );
    }
}
