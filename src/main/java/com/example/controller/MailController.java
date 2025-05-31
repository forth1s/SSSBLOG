package com.example.controller;

import com.example.common.utils.Util;
import com.example.entity.Result;
import com.example.service.MailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MailController {
    @Autowired
    private MailService mailService;

    @PostMapping("/bind-email")
    public Result<?> sendEmail(
            @RequestParam String email,
            HttpServletRequest request
    ) {
        String businessType = Util.extractBusinessType(request);
        mailService.sendEmail(email, businessType);
        return Result.success("验证码已发送");
    }
}
