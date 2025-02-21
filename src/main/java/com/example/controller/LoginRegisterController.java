package com.example.controller;

import com.example.entity.Result;
import com.example.entity.User;
import com.example.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by sang on 2017/12/17.
 */
@RestController
public class LoginRegisterController {

    final UserService userService;

    public LoginRegisterController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/login_error")
    public Result loginError() {
        return new Result("error", "登录失败!");
    }

    @RequestMapping("/login_success")
    public Result loginSuccess() {
        return new Result("success", "登录成功!");
    }

    /**
     * 如果自动跳转到这个页面，说明用户未登录，返回相应的提示即可
     * 如果要支持表单登录，可以在这个方法中判断请求的类型，进而决定返回JSON还是HTML页面
     */
    @RequestMapping("/login_page")
    public Result loginPage() {
        return new Result("error", "尚未登录，请登录!");
    }

    @PostMapping("/register")
    public Result reg(User user) {
        int result = userService.reg(user);
        if (result == 0) {
            //成功
            return new Result("success", "注册成功!");
        } else if (result == 1) {
            return new Result("error", "用户名重复，注册失败!");
        } else {
            //失败
            return new Result("error", "注册失败!");
        }
    }
}
