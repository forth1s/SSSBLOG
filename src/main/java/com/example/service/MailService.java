package com.example.service;

import com.example.entity.User;
import com.example.common.utils.CaptchaUtil;
import com.example.common.utils.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MailService {
    @Value("${spring.mail.username}")
    private String mailHost;
    private final UserService userService;
    private final JavaMailSender mailSender;
    private final RedisUtil redisUtil;
    private final static int MAIL_CODE_SIZE = 6;

    private final CaptchaUtil captchaUtil = CaptchaUtil.newBuilder().setSize(MAIL_CODE_SIZE).build();

    public MailService(UserService userService, JavaMailSender mailSender, RedisUtil redisUtil) {
        this.userService = userService;
        this.mailSender = mailSender;
        this.redisUtil = redisUtil;
    }

    public void sendEmail(String email, String title, String key) throws Exception {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new Exception("该邮箱未注册");
        }
        Object code = captchaUtil.createImage()[0];
        redisUtil.set(key, code, 60 * 60);
        // 使用 DateTimeFormatter 进行日期格式化
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 格式化时间
        String time = now.format(formatter);
        MimeMessage mimeMessage;
        MimeMessageHelper helper;
        try {
            //发送复杂的邮件
            mimeMessage = mailSender.createMimeMessage();
            //组装
            helper = new MimeMessageHelper(mimeMessage, true);
            //邮件标题
            helper.setSubject(title);
            //因为设置了邮件格式所以html标签有点多，后面的ture为支持识别html标签
            helper.setText("<h3>\n" +
                            "\t<span style=\"font-size:16px;\">亲爱的用户：</span> \n" +
                            "</h3>\n" +
                            "<p>\n" +
                            "\t<span style=\"font-size:14px;\">&nbsp;&nbsp;&nbsp;&nbsp;</span><span style=\"font-size:14px;\">&nbsp; <span style=\"font-size:16px;\">&nbsp;&nbsp;您好！您正在进行邮箱验证，本次请求的验证码为：<span style=\"font-size:24px;color:#FFE500;\"> "
                            + code +
                            "</span>,&nbsp;本验证码1小时内有效，请在1小时内完成验证。（请勿泄露此验证码）如非本人操作，请忽略该邮件。(这是一封自动发送的邮件，请不要直接回复）</span></span>\n" +
                            "</p>\n" +
                            "<p style=\"text-align:right;\">\n" +
                            "\t<span style=\"background-color:#FFFFFF;font-size:16px;color:#000000;\"><span style=\"color:#000000;font-size:16px;background-color:#FFFFFF;\"><span class=\"token string\" style=\"font-family:&quot;font-size:16px;color:#000000;line-height:normal !important;background-color:#FFFFFF;\">SSSBlog</span></span></span> \n" +
                            "</p>\n" +
                            "<p style=\"text-align:right;\">\n" +
                            "\t<span style=\"background-color:#FFFFFF;font-size:14px;\"><span style=\"color:#FF9900;font-size:18px;\"><span class=\"token string\" style=\"font-family:&quot;font-size:16px;color:#000000;line-height:normal !important;\"><span style=\"font-size:16px;color:#000000;background-color:#FFFFFF;\">"
                            + time +
                            "</span><span style=\"font-size:18px;color:#000000;background-color:#FFFFFF;\"></span></span></span></span> \n" +
                            "</p>", true);
            //收件人
            helper.setTo(email);
            //发送方
            helper.setFrom(mailHost);
            try {
                //发送邮件
                mailSender.send(mimeMessage);
            } catch (MailException e) {
                //邮箱是无效的，或者发送失败
                throw new Exception("邮件发送失败");
            }
        } catch (MessagingException e) {
            //发送失败--服务器繁忙
            throw new Exception("服务器繁忙");
        }
    }
}
