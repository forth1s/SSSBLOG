package com.example.security;

import com.example.security.filters.CaptchaFilter;
import com.example.security.filters.JwtAuthenticationFilter;
import com.example.entity.Result;
import com.example.security.handlers.SecurityExceptionHandler;
import com.example.service.UserService;
import com.example.common.utils.RedisUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;

/**
 * 想让 Spring Security 中的资源可以匿名访问时，有两种办法：
 * 1、走 Spring Security 过滤器链，但是可以匿名访问。
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String[] PERMIT_ALL_PATHS = {
            "/captcha",
            "/login",
            "/register",
            "/reset-password",
            "/forgot-password",
            "/error",
    };

    private static final String ADMIN_ROLE = "超级管理员";

    private final UserService userService;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;
    private final SecurityExceptionHandler exceptionHandler;


    public SecurityConfig(
            UserService userService,
            RedisUtil redisUtil,
            ObjectMapper objectMapper,
            SecurityExceptionHandler exceptionHandler
    ) {
        this.userService = userService;
        this.redisUtil = redisUtil;
        this.objectMapper = objectMapper;
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * 配置认证类
     */
    @Bean
    public AuthenticationManager authenticationManager(SaltyPasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        //将编写的UserDetailsService注入进来
        provider.setUserDetailsService(userService);
        //将使用的密码编译器加入进来
        provider.setPasswordEncoder(passwordEncoder);
        //将provider放置到AuthenticationManager 中
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 无状态会话（JWT 场景，前后端分离增强）
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .requestMatchers("/admin/**").hasAuthority(ADMIN_ROLE)
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 登出接口
                        .deleteCookies("JSESSIONID") // 清除 Cookie
                        .logoutSuccessHandler(
                                (_, response, _)
                                        -> sendResponse(response, Result.success("注销成功"))
                        )
                        .permitAll()
                )
                // 过滤器顺序：验证码过滤器（登录时验证） -> JWT 认证过滤器（所有请求解析 Token） -> 用户名密码认证过滤器
                .addFilterBefore(new CaptchaFilter(redisUtil, exceptionHandler), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(userService, exceptionHandler), UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                // 只能处理 Spring Security 框架内部抛出的异常
                .exceptionHandling(
                        handling -> handling
                                .accessDeniedHandler(exceptionHandler)
                                .authenticationEntryPoint(exceptionHandler) // 使用委托的 EntryPoint
                );
        return http.build();
    }

    // 通用响应方法
    // 因为过滤器工作时未进入Controller层，spring不会对Result进行序列化，所以要自己处理响应
    private <T> void sendResponse(HttpServletResponse response, Result<T> result) throws IOException {
        response.setStatus(result.getCode());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), result);
    }

    /**
     * 跨域配置
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*"); // 允许所有来源（生产环境需限制具体域名）
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
