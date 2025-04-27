package com.example.config;

import com.example.common.utils.ResponseUtil;
import com.example.service.UserService;
import com.example.common.utils.RedisUtil;

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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static com.example.common.utils.JwtTokenUtil.generateToken;

/**
 * 想让 Spring Security 中的资源可以匿名访问时，有两种办法：
 * 1、走 Spring Security 过滤器链，但是可以匿名访问。
 */
@Component
@Configuration
@EnableWebSecurity
// 启用方法级别的权限认证
public class WebSecurityConfig {
    private static final String[] PERMIT_ALL_PATHS = {
            "/getcode",
            "/register",
            "/sendMail",
            "/reset-password"
    };

    private static final String ADMIN_ROLE = "超级管理员";

    private final UserService userService;
    private final RedisUtil redisUtil;

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
                                            String username = authentication.getName();
                                            try {
                                                String token = generateToken(username);
                                                ResponseUtil.sendSuccessResponse(httpServletResponse, "登录成功", token);
                                            } catch (Exception e) {
                                                ResponseUtil.sendServerErrorResponse(httpServletResponse, e.getMessage());
//                                        throw new ServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                                            }
                                        }
                                )
                                .failureHandler(
                                        (_, httpServletResponse, e) -> {
                                            ResponseUtil.sendUnauthorizedResponse(httpServletResponse, e.getMessage());
                                        }
                                )
                                .permitAll() // 允许所有用户访问登录页面
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 登出接口
//                        .invalidateHttpSession(true) // 由于采用无状态模式，登出时服务器无需销毁 Session（invalidateHttpSession实际无效）
//                        .deleteCookies("JSESSIONID") // 清除 Cookie
//                        .permitAll()
                        .logoutSuccessHandler((_, httpServletResponse, _) ->
                                ResponseUtil.sendSuccessResponse(httpServletResponse, "登出成功"))
                )
                // 过滤器顺序：验证码过滤器（登录时验证） -> JWT 认证过滤器（所有请求解析 Token） -> 用户名密码认证过滤器
                .addFilterBefore(new CaptchaFilter(redisUtil), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(userService), UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(
                        exception -> exception
                                .accessDeniedHandler(new NotEnoughAccessDeniedHandler())
                                .authenticationEntryPoint((_, httpServletResponse, e) -> {
                                            System.out.println("CaptchaFilter Excepti十多个房价大幅高开杀个发on: " + e);
                                            ResponseUtil.sendUnauthorizedResponse(httpServletResponse, e.getMessage());
//                               throw new UnauthorizedException(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
                                        }
                                )
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