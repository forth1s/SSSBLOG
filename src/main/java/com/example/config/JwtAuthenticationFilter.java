package com.example.config;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.service.UserService;
import com.example.utils.JwtTokenUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";

    // 跳过不需要进行 JWT 认证的接口路径
    private static final String[] AUTH_WHITELIST = {
            "/login",
            "/register",
            "/sendMail",
            "/reset-password",
            "/getcode"
    };

    private final UserService userService;

    public JwtAuthenticationFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws IOException, ServletException {
        // 1. 跳过公共接口（避免循环认证）
        String uri = request.getRequestURI();
        for (String path : AUTH_WHITELIST) {
            if (uri.equals(path)) {
                filterChain.doFilter(request, response);
                return;
            }
        }
        // 2. 从请求头获取 Token
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 提取 Token（去除 Bearer 前缀）
        String token = authHeader.replace(TOKEN_PREFIX, "").trim();

        try {
            // 4. 验证 Token 并获取用户名
            DecodedJWT decodedJWT = JwtTokenUtil.validateToken(token);
            String username = decodedJWT.getSubject();

            // 5. 从 UserService 加载用户详情（包含角色信息）
            UserDetails userDetails = userService.loadUserByUsername(username);

            // 6. 创建 Authentication 对象并设置到 SecurityContext
            if (userDetails != null && userDetails.isEnabled()) { // 检查用户是否可用
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities() // 从 UserDetails 中获取角色权限
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (TokenExpiredException e) {
            handleTokenException(response, HttpServletResponse.SC_UNAUTHORIZED, "Token已过期，请重新登录");
            return;
        } catch (JWTDecodeException e) {
            handleTokenException(response, HttpServletResponse.SC_UNAUTHORIZED, "Token解析错误，请检查Token");
            return;
        } catch (Exception e) {
            handleTokenException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Token验证发生未知错误");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleTokenException(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}