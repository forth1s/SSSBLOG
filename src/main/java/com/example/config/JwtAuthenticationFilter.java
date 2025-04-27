package com.example.config;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.common.exception.ServerException;
import com.example.common.exception.UnauthorizedException;
import com.example.service.UserService;
import com.example.common.utils.JwtTokenUtil;
import com.example.common.utils.ResponseUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";

//    private static final String[] AUTH_WHITELIST = {
//            "/login",
//            "/register",
//            "/sendMail",
//            "/reset-password",
//            "/getcode"
//    };

    private final UserService userService;

    public JwtAuthenticationFilter(UserService userService) {
        this.userService = userService;
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
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JWTVerificationException e) {
                ResponseUtil.sendUnauthorizedResponse(response, e.getMessage());
//            throw new UnauthorizedException(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            } catch (UsernameNotFoundException e) { // 显式捕获用户不存在异常
                ResponseUtil.sendUnauthorizedResponse(response,e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}