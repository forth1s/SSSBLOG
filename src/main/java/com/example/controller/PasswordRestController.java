package com.example.controller;

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
    private static final String PASSWORD_RESET_BY_EMAIL_PREFIX = "PASSWORD_RESET_BY_EMAIL_";

    public PasswordRestController(UserService userService, MailService mailService) {
        this.userService = userService;
        this.mailService = mailService;
    }


    @PostMapping("/sendMail")
    public Result<?> sendEmail(@RequestParam String email, HttpServletRequest request) {
        String title="【SSSBlog】 重置密码验证码";
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        String verifyCodeKey = PASSWORD_RESET_BY_EMAIL_PREFIX + sessionId;
//        String verifyCodeKey = EMAIL_RESET_CODE_PREFIX + email;
        try {
            mailService.sendEmail(email, title ,verifyCodeKey);
            return new Result<>(200,"success", "验证码已发送");
        }catch (Exception e){
            return new Result<>(500,"error", e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public Result<?> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        try{
        if ((userService.updatePasswordByEmail(email, newPassword))==1)
            return new Result<>(204,"success", "密码重置成功");
        else {
            return new Result<>(500,"error", "密码重置失败");
        }}catch (Exception e){
            return new Result<>(500,"error", "重置密码时发生错误，请稍后再试");
        }
    }
}
//@RequestParam String code, @RequestParam String newPassword