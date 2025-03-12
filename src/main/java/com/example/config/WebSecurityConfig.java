package com.example.config;

//import com.example.controller.VerifyController;

import com.example.entity.Result;
import com.example.service.UserService;
import com.example.utils.RedisUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;

/**
 * 想让 Spring Security 中的资源可以匿名访问时，有两种办法：
 * 1、走 Spring Security 过滤器链，但是可以匿名访问。
 */
@Configuration
@EnableWebSecurity
// 启用方法级别的权限认证
public class WebSecurityConfig {
    final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisUtils redisUtils;

    public WebSecurityConfig(UserService userService, RedisUtils redisUtils) {

        this.userService = userService;
        this.redisUtils = redisUtils;
    }

    /**
     * 配置认证类
     */
    @Bean
    public AuthenticationManager authenticationManager(MyPasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        //将编写的UserDetailsService注入进来
        provider.setUserDetailsService(userService);
        //将使用的密码编译器加入进来
        provider.setPasswordEncoder(passwordEncoder);
        //将provider放置到AuthenticationManager 中
        return new ProviderManager(provider);
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/getcode","/login_error", "/register").permitAll()
                        .requestMatchers("/static/**").permitAll()  // 允许访问静态资源
                        //.requestMatchers("/admin/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("超级管理员")
                        .anyRequest().authenticated()
                )
                .formLogin(fLogin -> fLogin
                                .loginPage("/login.html") // 指定登录页面
                                .loginProcessingUrl("/login")
//                        .successHandler(
//                                (_, httpServletResponse, _) -> {
//                                    httpServletResponse.setContentType("application/json;charset=utf-8");
//                                    Result result = new Result("success", "登录成功");
//                                    String jsonResponse = objectMapper.writeValueAsString(result);
//                                    try (PrintWriter out = httpServletResponse.getWriter()) {
//                                        out.write(jsonResponse);
//                                        out.flush();
//                                    }
//                                }
//                        )
                                .successForwardUrl("/login_success")
//                        .failureHandler(
//                                (_, httpServletResponse, _) -> {
//                                    httpServletResponse.setContentType("application/json;charset=utf-8");
//                                    Result result = new Result("error", "登录失败");
//                                    String jsonResponse = objectMapper.writeValueAsString(result);
//                                    try (PrintWriter out = httpServletResponse.getWriter()) {
//                                        out.write(jsonResponse);
//                                        out.flush();
//                                    }
//                                }
//                        )
                                // failureForwardUrl方法的跳转是服务器端跳转，
                                // 服务器端跳转的好处是可以携带登录异常信息。如果登录失败，自动跳转到登录页面后，就可以将错误信息展示出来。
                                .failureForwardUrl("/login_error")
                                .usernameParameter("username")
                                .passwordParameter("password")
                                .permitAll() // 允许所有用户访问登录页面
                )
                .logout(LogoutConfigurer::permitAll)
                .addFilterBefore(new CaptchaFilter(redisUtils), UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.accessDeniedHandler(getAccessDeniedHandler()));
        return http.build();
    }

    // 2、用于自定义 Web 安全配置，忽略指定的请求路径，这些请求将不会经过 Spring Security 的过滤器链。
//    @Bean
//    WebSecurityCustomizer webSecurityCustomizer() {
//        return web -> web.ignoring().requestMatchers(

//                "/static/**",
//                "/templates/**",
//                "/head/**"
//        );
//    }

    /**
     * 用于处理用户访问被拒绝的情况
     */
    @Bean
    AccessDeniedHandler getAccessDeniedHandler() {
        return (_, response, _) -> {
            response.setContentType("application/json;charset=utf-8");
            Result result = new Result("error", "访问被拒绝");
            String jsonResponse = objectMapper.writeValueAsString(result);
            try (PrintWriter out = response.getWriter()) {
                out.write(jsonResponse);
                out.flush();
            }
        };
    }
}