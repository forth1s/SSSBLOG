package com.example.controller;

import com.example.utils.VerifyUtil;
import com.example.entity.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import com.example.utils.RedisUtils;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("verify")
public class VerifyController {

    private final RedisUtils redisTemplate;

    // 验证码最大尝试次数
    private static final int MAX_ATTEMPTS = 1;
    // 验证码有效期（分钟）
    private static final int VERIFY_CODE_EXPIRE_MINUTES = 2;

    public VerifyController(RedisUtils redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成验证码的接口
     *
     * @param request Request对象
     * @return 统一响应格式的结果
     */
    @PostMapping("/getcode")
    public Result getCode(HttpServletRequest request) throws Exception {
        // 获取到session
        HttpSession session = request.getSession();
        // 取到sessionid
        String id = session.getId();

        // 检查请求频率
        if (isRequestTooFrequent(id)) {
            return new Result("error", "请求过于频繁，请稍后再试");
        }

        // 利用图片工具生成图片
        // 返回的数组第一个参数是生成的验证码，第二个参数是生成的图片
        Object[] objs = VerifyUtil.newBuilder()
                .setWidth(120)
                .setHeight(35)
                .setSize(6)
                .setLines(10)
                .setFontSize(25)
                .setTilt(true)
                .setBackgroundColor(Color.LIGHT_GRAY)
                .build()
                .createImage();

        // 将验证码存入Session
        session.setAttribute("CAPTCHA", objs[0]);

        // 打印验证码（仅用于调试，生产环境可移除）
        System.out.println(objs[0]);

        // 在redis中保存验证码尝试次数
        redisTemplate.set(("VERIFY_CODE_ATTEMPTS_" + id), String.valueOf(MAX_ATTEMPTS), VERIFY_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 更新请求时间
        updateRequestTime(id);

        // 将图片转换为 Base64 编码
        BufferedImage image = (BufferedImage) objs[1];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        return new Result("success", "data:image/png;base64," + base64Image);
    }

    /**
     * 业务接口包含了验证码的验证
     *
     * @param code    前端传入的验证码
     * @param request Request对象
     * @return 统一响应格式的结果
     */
    @GetMapping("/checkcode")
    public Result checkCode(String code, HttpServletRequest request) {
        HttpSession session = request.getSession();
        String id = session.getId();

        // 获取验证码尝试次数
        String attemptsKey = "VERIFY_CODE_ATTEMPTS_" + id;
        String attemptsStr = (String) redisTemplate.get(attemptsKey);
        if (!StringUtils.hasLength(attemptsStr)) {
            return new Result("error", "验证码已失效，请重新获取");
        }
        int attempts = Integer.parseInt(attemptsStr);

        // 检查尝试次数是否超过限制
        if (attempts <= 0) {
            redisTemplate.delete(attemptsKey);
            session.removeAttribute("CAPTCHA");
            return new Result("error", "验证码尝试次数已用完，请重新获取");
        }

        // 将session中的取出对应session id生成的验证码
        String serverCode = (String) session.getAttribute("CAPTCHA");
        // 校验验证码（大小写不敏感）
        if (serverCode == null || !serverCode.equalsIgnoreCase(code)) {
            // 尝试次数减一
            redisTemplate.decrement(attemptsKey);
            return new Result("error", "验证码输入错误，还剩 " + (attempts - 1) + " 次尝试机会");
        }

        // 验证通过之后手动将验证码失效
        redisTemplate.delete(attemptsKey);
        session.removeAttribute("CAPTCHA");

        return new Result("success", "验证码验证通过");
    }

    /**
     * 检查请求是否过于频繁
     *
     * @param id session id
     * @return 是否过于频繁
     */
    private boolean isRequestTooFrequent(String id) {
        String lastRequestTimeKey = "VERIFY_CODE_LAST_REQUEST_" + id;
        String lastRequestTimeStr = (String) redisTemplate.get(lastRequestTimeKey);
        if (!StringUtils.hasLength(lastRequestTimeStr)) {
            return false;
        }
        long lastRequestTime = Long.parseLong(lastRequestTimeStr);
        long currentTime = System.currentTimeMillis();
        // 限制每10秒只能请求一次
        return currentTime - lastRequestTime < 10000;
    }

    /**
     * 更新请求时间
     *
     * @param id session id
     */
    private void updateRequestTime(String id) {
        String lastRequestTimeKey = "VERIFY_CODE_LAST_REQUEST_" + id;
        redisTemplate.set(lastRequestTimeKey, String.valueOf(System.currentTimeMillis()), 10, TimeUnit.SECONDS);
    }
}