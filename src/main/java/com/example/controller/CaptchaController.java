package com.example.controller;

import com.example.common.utils.CaptchaUtil;
import com.example.entity.Result;
import com.example.common.utils.RedisUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.util.StringUtils;

@RestController
public class CaptchaController {

    private final RedisUtil redisUtil;

    // 验证码刷新间隔（毫秒）
    private static final int REFRESH_INTERVAL = 60*1000;
    // 验证码有效期（分钟）
    private static final int VERIFY_CODE_EXPIRE_MINUTES = 2;

    private static final String VERIFY_CODE_PREFIX = "VERIFY_CODE_";
    private static final String LAST_REQUEST_PREFIX = "LAST_REQUEST_";
    public CaptchaController(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /**
     * 生成验证码的接口
     *
     * @return 统一响应格式的结果
     */
    @RequestMapping("/getcode")
    public Result<?> getCode() {
        try {

            String uuid = UUID.randomUUID().toString(); // 生成全局唯一UUID

            if (isRequestTooFrequent(uuid)) {
                return new Result<>(429, "请求过于频繁，请稍后再试", null);
            }

            // 使用CaptchaUtil默认属性值（无需显式设置参数）
            Object[] objs = CaptchaUtil.newBuilder()
                    .build()
                    .createImage();

            System.out.println(objs[0]);

            redisUtil.set((VERIFY_CODE_PREFIX + uuid), objs[0], VERIFY_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            updateRequestTime(uuid);

            BufferedImage image = (BufferedImage) objs[1];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            return new Result<>(200, "success","image/png;base64," + base64Image);
        } catch (Exception e) {
            return new Result<>(500,"error", "生成验证码错误："+e.getMessage());
        }
    }

    /**
     * 检查请求是否过于频繁
     *
     * @param id session id
     * @return 是否过于频繁
     */
    private boolean isRequestTooFrequent(String id) {
        String lastRequestTimeKey = LAST_REQUEST_PREFIX + id;
        String lastRequestTimeStr = (String) redisUtil.get(lastRequestTimeKey);
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
        String lastRequestTimeKey = LAST_REQUEST_PREFIX + id;
        redisUtil.set(lastRequestTimeKey, String.valueOf(System.currentTimeMillis()), 10, TimeUnit.SECONDS);
    }
}