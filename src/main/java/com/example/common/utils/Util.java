package com.example.common.utils;

import com.example.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class Util {
    private Util() {

    }
    public static User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}