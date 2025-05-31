package com.example.service;

import com.example.common.enums.BusinessType;
import com.example.common.enums.CaptchaBusinessType;
import com.example.common.exceptions.BadRequestException;
import com.example.common.exceptions.ServerException;
import com.example.common.utils.CaptchaUtil;
import com.example.common.utils.RedisUtil;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class CaptchaService {

    private final RedisUtil redisUtil;

    public CaptchaService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public String generateCaptcha(String captchaId, String businessType){

        CaptchaBusinessType type = BusinessType.fromName(businessType, CaptchaBusinessType.class);

        if (redisUtil.isRequestFrequent(captchaId, type, type.getExpireTimeInterval())) {
            throw new BadRequestException(400, "请求过于频繁，请稍后再试");
        }

        CaptchaUtil captchaUtil = CaptchaUtil.newBuilder().build();
        String code = captchaUtil.generateCode();

        String redisKey = type.getPrefix() + captchaId;
        redisUtil.set(redisKey, code, type.getExpireTime(), type.getTimeUnit());
        redisUtil.updateRequestTime(type, captchaId);

        if (type == CaptchaBusinessType.LOGIN
                || type == CaptchaBusinessType.REGISTER
                || type == CaptchaBusinessType.SEND_EMAIL
                || type == CaptchaBusinessType.FORGOT_PASSWORD
        ) {
            BufferedImage image = captchaUtil.createImage(code);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try{
                ImageIO.write(image, "png", baos);
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
            } catch (IOException e) {
                throw new ServerException(500,e.getMessage());
            }
        } else {
            return code;
        }
    }
}
