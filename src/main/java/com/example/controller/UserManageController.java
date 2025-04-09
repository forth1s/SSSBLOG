package com.example.controller;

import com.example.entity.Result;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by sang on 2017/12/24.
 */
@RestController
@RequestMapping("/admin")
public class UserManageController {
    final UserService userService;

    public UserManageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/user")
    public List<User> getUsersByUsername(String username) {
        return userService.getUsersByUsername(username);
    }

    @GetMapping(value = "/user/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping(value = "/roles")
    public List<Role> getAllRole() {
        return userService.getAllRole();
    }

    @PutMapping(value = "/user/enabled")
    public Result updateUserEnabled(Boolean enabled, Long uid) {
        if (userService.updateUserEnabled(enabled, uid) == 1) {
            return new Result("success", "更新成功!");
        } else {
            return new Result("error", "更新失败!");
        }
    }

    @DeleteMapping(value = "/user/{uid}")
    public Result deleteUserById(@PathVariable Long uid) {
        if (userService.deleteUserById(uid) == 1) {
            return new Result("success", "删除成功!");
        } else {
            return new Result("error", "删除失败!");
        }
    }

    @PutMapping(value = "/user/role")
    public Result updateUserRoles(Long[] rids, Long id) {
        if (userService.updateUserRoles(rids, id) == rids.length) {
            return new Result("success", "更新成功!");
        } else {
            return new Result("error", "更新失败!");
        }
    }
}