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

    @PostMapping("/register")
    public Result reg(User user) {
        int flag = userService.reg(user);
        if (flag == 0) {
            //成功
            return new Result("success", "注册成功!");
        } else if (flag == 1) {
            return new Result("error", "用户名重复，注册失败!");
        } else {
            //失败
            return new Result("error", "注册失败!");
        }
    }
}
