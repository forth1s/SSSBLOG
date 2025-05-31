package com.example.security.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.security.handlers.SecurityExceptionHandler;
import com.example.service.UserService;
import com.example.common.utils.JwtTokenUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserService userService;
    private final SecurityExceptionHandler exceptionHandler;

    public JwtAuthenticationFilter(UserService userService, SecurityExceptionHandler exceptionHandler) {
        this.userService = userService;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws IOException, ServletException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                DecodedJWT decodedJWT = JwtTokenUtil.validateToken(token);
                String username = decodedJWT.getSubject();

                UserDetails userDetails = userService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JWTVerificationException e) {
                // 重置安全上下文，确保没有残留的认证信息
                SecurityContextHolder.clearContext();
                exceptionHandler.commence(request,response,new AuthenticationServiceException(e.getMessage()));
            }
        }
        filterChain.doFilter(request, response);
    }
}
