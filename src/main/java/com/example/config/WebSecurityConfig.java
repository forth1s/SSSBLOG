package com.example.config;

import com.example.entity.Result;
import com.example.service.UserService;
import com.example.utils.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.PrintWriter;

import static com.example.utils.JwtTokenUtil.generateToken;

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
    private final RedisUtil redisUtil;

    private static final String[] PERMIT_ALL_PATHS = {
            "/getcode",
            "/register",
            "/sendMail",
            "/reset-password"
    };

    // 若角色无 ROLE_ 前缀，需在 UserDetails 中返回正确角色（如 ROLE_超级管理员）
    private static final String ADMIN_ROLE = "超级管理员";


    public WebSecurityConfig(UserService userService, RedisUtil redisUtil) {
        this.userService = userService;
        this.redisUtil = redisUtil;
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

        http.sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 无状态会话（JWT 场景，前后端分离增强）
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .requestMatchers("/admin/**").hasAuthority(ADMIN_ROLE)
                        .anyRequest().authenticated()
                )
                .formLogin(fLogin -> fLogin
                        .loginProcessingUrl("/login") // 登录接口
                        .successHandler(
                                (_, httpServletResponse, authentication) -> {
                                    httpServletResponse.setContentType("application/json;charset=utf-8");
                                    // 添加生成 Token 的逻辑
                                    String username = authentication.getName();
                                    String token = generateToken(username);
                                    System.out.println(token);
                                    Result result = new Result("success", "token:" + token);
                                    try (PrintWriter out = httpServletResponse.getWriter()) {
                                        out.write(objectMapper.writeValueAsString(result));
                                        out.flush();
                                    }
                                }
                        )
                        .failureHandler(
                                (_, httpServletResponse, _) -> {
                                    httpServletResponse.setContentType("application/json;charset=utf-8");
                                    Result result = new Result("error", "登录失败");
                                    try (PrintWriter out = httpServletResponse.getWriter()) {
                                        out.write(objectMapper.writeValueAsString(result));
                                        out.flush();
                                    }
                                }
                        )
                        .permitAll() // 允许所有用户访问登录页面
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 登出接口
                        .invalidateHttpSession(true) // 失效 Session
                        .deleteCookies("JSESSIONID") // 清除 Cookie
                        .permitAll()
                )
                // 过滤器顺序：验证码过滤器（登录时验证） -> JWT 认证过滤器（所有请求解析 Token） -> 用户名密码认证过滤器
                .addFilterBefore(new CaptchaFilter(redisUtil), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(userService), UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(getAccessDeniedHandler())
                        .authenticationEntryPoint((_, response, _) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=utf-8");
                            Result result = new Result("error", "未认证，请先登录");
                            try (PrintWriter out = response.getWriter()) {
                                out.write(objectMapper.writeValueAsString(result));
                            }
                        })
                );
        return http.build();
    }

    // 2、用于自定义 Web 安全配置，忽略指定的请求路径，这些请求将不会经过 Spring Security 的过滤器链。
    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/static/**",
                "/templates/**"
        );
    }

    /**
     * 用于处理用户访问被拒绝的情况
     */
    @Bean
    AccessDeniedHandler getAccessDeniedHandler() {
        return (_, response, _) -> {
            response.setStatus(403);
            response.setContentType("application/json;charset=utf-8");
            Result result = new Result("error", "权限不足，禁止访问");
            try (PrintWriter out = response.getWriter()) {
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
            }
        };
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