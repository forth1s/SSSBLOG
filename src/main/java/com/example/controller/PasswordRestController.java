package com.example.controller;

import com.example.common.utils.Util;
import com.example.entity.Result;
import com.example.service.MailService;
import com.example.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PasswordRestController {
    private final UserService userService;
    private final MailService mailService;

    public PasswordRestController(UserService userService, MailService mailService) {
        this.userService = userService;
        this.mailService = mailService;
    }

    /**
     * 发送更改密码请求的邮件
     */
    @PostMapping("/forgot-password")
    public Result<?> forgotPassword(HttpServletRequest request) {
        String businessType = Util.extractBusinessType(request);
        String email = request.getParameter("email");
        mailService.sendEmail(email, businessType);
        return Result.success("邮件已发送");
    }

    /**
     */
    @PostMapping("/reset-password")
    public Result<?> resetPassword(HttpServletRequest request) {
        String email = request.getParameter("email");
        String newPassword = request.getParameter("newPassword");
        userService.updatePasswordByEmail(email, newPassword);
        return Result.success("密码重置成功");
    }
}
