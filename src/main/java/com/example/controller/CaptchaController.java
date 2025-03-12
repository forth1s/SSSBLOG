package com.example.controller;

import com.example.utils.VerifyUtil;
import com.example.entity.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
public class CaptchaController {

    private final RedisUtils redisTemplate;

    // 验证码刷新间隔（毫秒）
    private static final int REFRESH_INTERVAL = 60*1000;
    // 验证码有效期（分钟）
    private static final int VERIFY_CODE_EXPIRE_MINUTES = 2;

    public CaptchaController(RedisUtils redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成验证码的接口
     *
     * @param request Request对象
     * @return 统一响应格式的结果
     */
    @RequestMapping("/getcode")
    public Result getCode(HttpServletRequest request) throws Exception {
        // 获取到session
        HttpSession session = request.getSession();
        // 取到sessionId
        String sessionId = session.getId();

        // 检查请求频率
        if (isRequestTooFrequent(sessionId)) {
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

        // 打印验证码（仅用于调试，生产环境可移除）
        System.out.println(objs[0]);

        // 在redis中保存验证码，设置有效期
        redisTemplate.set(("VERIFY_CODE_" + sessionId), objs[0], VERIFY_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 更新请求时间
        updateRequestTime(sessionId);

        // 将图片转换为 Base64 编码
        BufferedImage image = (BufferedImage) objs[1];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        return new Result("success", "data:image/png;base64," + base64Image);
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
        // 限制每REFRESH_INTERVAL只能请求一次
        return currentTime - lastRequestTime < REFRESH_INTERVAL;
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