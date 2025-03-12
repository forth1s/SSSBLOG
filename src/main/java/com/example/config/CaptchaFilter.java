//package com.example.config;
//
//import com.example.entity.Result;
//import com.example.utils.RedisUtils;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.annotation.Nonnull;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//
//public class CaptchaFilter extends OncePerRequestFilter {
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    private final RedisUtils redisUtils;
//    public CaptchaFilter(RedisUtils redisUtils) {
//        this.redisUtils = redisUtils;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain)
//            throws ServletException, IOException {
//        // 只对登录请求进行验证码校验
//        if (("/login".equals(request.getRequestURI()) || "register".equals(request.getRequestURI()))
//                && "POST".equals(request.getMethod())) {
//            String inputCaptcha = request.getParameter("captcha");
//            String sessionId = request.getSession().getId();
//            String correctCaptcha = (String) redisUtils.get("VERIFY_CODE"+sessionId);
//
//            if (inputCaptcha == null ||!inputCaptcha.equalsIgnoreCase(correctCaptcha)) {
//                // 构造统一响应格式
//                Result result = new Result("error", "验证码错误!");
//                // 设置响应状态码为 400
//                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                // 设置响应内容类型为 JSON 并指定字符编码
//                response.setContentType("application/json;charset=UTF-8");
//                // 将 Result 对象转换为 JSON 字符串
//                String jsonResponse = objectMapper.writeValueAsString(result);
//                // 获取 PrintWriter 并将 JSON 响应写入响应体
//                try (PrintWriter out = response.getWriter()) {
//                    out.print(jsonResponse);
//                    out.flush();
//                }
//                return;
//            }
//        }
//        // 继续执行后续过滤器
//        filterChain.doFilter(request, response);
//    }
//}

package com.example.config;

import com.example.entity.Result;
import com.example.utils.RedisUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

public class CaptchaFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisUtils redisUtils;

    public CaptchaFilter(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {
        // 处理登录和注册请求
        if (("/login".equals(request.getRequestURI()) || "/register".equals(request.getRequestURI()))
                && "POST".equals(request.getMethod())) {
            Result result = new Result();
            HttpSession session = request.getSession();
            String sessionId = session.getId();
            String verifyCodeKey = "VERIFY_CODE_" + sessionId;
            try {
                String inputCaptcha = request.getParameter("captcha");
                if (!StringUtils.hasLength(inputCaptcha)) {
                    throw new AuthenticationException("验证码不能为空") {
                        @Override
                        public String getMessage() {
                            return "验证码不能为空";
                        }
                    };
                }

                String correctCaptcha = (String) redisUtils.get(verifyCodeKey);

                // 检查验证码是否已过期
                if (!StringUtils.hasLength(correctCaptcha)) {
                    throw new AuthenticationException("验证码已失效，请重新获取") {
                        @Override
                        public String getMessage() {
                            return "验证码已失效，请重新获取";
                        }
                    };
                }

                // 校验验证码（大小写不敏感）
                if (!correctCaptcha.equalsIgnoreCase(inputCaptcha.trim())) {
                    throw new AuthenticationException("验证码输入错误，请重新获取") {
                        @Override
                        public String getMessage() {
                            return "验证码输入错误，请重新获取";
                        }
                    };
                }

                // 验证通过，删除验证码信息
                redisUtils.delete(verifyCodeKey);

                result.setStatus("success");
                result.setMsg("验证码验证通过");
            } catch (AuthenticationException e) {
                // 无论验证是否通过，都删除 Redis 中的验证码信息
                redisUtils.delete(verifyCodeKey);
                result.setStatus("error");
                result.setMsg(e.getMessage());
            }

            // 统一返回JSON格式
            response.setContentType("application/json;charset=utf-8");
            if ("success".equals(result.getStatus())) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            try (PrintWriter out = response.getWriter()) {
                out.write(objectMapper.writeValueAsString(result));
                out.flush();
            }
            return;
        }
        filterChain.doFilter(request, response);
    }
}