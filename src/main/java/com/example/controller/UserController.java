package com.example.controller;

import com.example.entity.Result;
import com.example.entity.User;
import com.example.service.UserService;
import com.example.common.utils.Util;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by sang on 2017/12/24.
 */
@RestController
public class UserController {

    final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/currentUserName")
    public String currentUserName() {
        return Util.getCurrentUser().getUsername();
    }

    @RequestMapping("/currentUserId")
    public Long currentUserId() {
        return Util.getCurrentUser().getId();
    }

    @RequestMapping("/currentUserEmail")
    public String currentUserEmail() {
        return Util.getCurrentUser().getEmail();
    }

    @RequestMapping("/isAdmin")
    public Boolean isAdmin() {
        List<GrantedAuthority> authorities = Util.getCurrentUser().getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().contains("超级管理员")) {
                return true;
            }
        }
        return false;
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestParam @NotBlank(message = "用户名不能为空") String username,
                                 @RequestParam @NotBlank(message = "密码不能为空") String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        userService.register(user);
        return Result.success("注册成功");
    }

    @PutMapping(value = "/updateUserEmail")
    public Result<Void> updateUserEmail(@RequestParam("email") String email) {
        userService.updateUserEmail(email); // 直接调用，不处理异常
        return Result.success("邮箱更新成功");
    }
}
