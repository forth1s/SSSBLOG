package com.example.service;

import com.example.common.enums.BusinessType;
import com.example.common.enums.EmailBusinessType;
import com.example.common.exceptions.BadRequestException;
import com.example.common.exceptions.ServerException;
import com.example.common.utils.DateTypeUtil;
import com.example.common.configs.RabbitMQConfig;
import com.example.entity.SerializableMailMessage;
import com.example.entity.User;
import com.example.common.utils.CaptchaUtil;
import com.example.common.utils.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Date;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
@Service
public class MailService {
    @Autowired
    private Environment environment;

    private final UserService userService;
    private final JavaMailSender mailSender;
    private final RedisUtil redisUtil;
    private final static int MAIL_CODE_SIZE = 6;

    private final RabbitTemplate rabbitTemplate;
    private final CaptchaUtil captchaUtil = CaptchaUtil.newBuilder().setSize(MAIL_CODE_SIZE).build();

    public MailService(UserService userService, JavaMailSender mailSender, RedisUtil redisUtil, RabbitTemplate rabbitTemplate) {
        this.userService = userService;
        this.mailSender = mailSender;
        this.redisUtil = redisUtil;
        this.rabbitTemplate = rabbitTemplate;
    }

    private String buildEmailContent(Object code, String time, String timeStamp) {
        // 构建邮件内容的逻辑，与之前相同
        return "<h3>\n" +
                "\t<span style=\"font-size:16px;\">亲爱的用户：</span> \n" +
                "</h3>\n" +
                "<p>\n" +
                "\t<span style=\"font-size:14px;\">&nbsp;&nbsp;&nbsp;&nbsp;</span><span style=\"font-size:14px;\">&nbsp; <span style=\"font-size:16px;\">&nbsp;&nbsp;您好！您正在进行邮箱验证，本次请求的验证码为：<span style=\"font-size:24px;color:#FFE500;\"> "
                + code +
                "</span>,&nbsp;本验证码" + time + "内有效，请在" + time + "内完成验证。（请勿泄露此验证码）如非本人操作，请忽略该邮件。(这是一封自动发送的邮件，请不要直接回复）</span></span>\n" +
                "</p>\n" +
                "<p style=\"text-align:right;\">\n" +
                "\t<span style=\"background-color:#FFFFFF;font-size:16px;color:#000000;\"><span style=\"color:#000000;font-size:16px;background-color:#FFFFFF;\"><span class=\"token string\" style=\"font-family:&quot;font-size:16px;color:#000000;line-height:normal !important;background-color:#FFFFFF;\">SSSBlog</span></span></span> \n" +
                "</p>\n" +
                "<p style=\"text-align:right;\">\n" +
                "\t<span style=\"background-color:#FFFFFF;font-size:14px;\"><span style=\"color:#FF9900;font-size:18px;\"><span class=\"token string\" style=\"font-family:&quot;font-size:16px;color:#000000;line-height:normal !important;\"><span style=\"font-size:16px;color:#000000;background-color:#FFFFFF;\">"
                + timeStamp +
                "</span><span style=\"font-size:18px;color:#000000;background-color:#FFFFFF;\"></span></span></span></span> \n" +
                "</p>";
    }

    public void sendEmail(String email, String businessType) {
        // 获取业务类型枚举
        EmailBusinessType business = BusinessType.fromName(businessType, EmailBusinessType.class);

        // 检查邮箱是否存在（注册业务除外）
        if (!businessType.equals("register")) {
            User user = userService.getUserByEmail(email);
            if (user == null) {
                throw new BadRequestException(HttpServletResponse.SC_BAD_REQUEST, "该邮箱未注册");
            }
        }

        String code = captchaUtil.generateCode();
        String redisKey = business.getPrefix() + email;

        // 设置Redis过期时间
        long expireSeconds = business.getTimeUnit().toSeconds(business.getExpireTime());
        redisUtil.set(redisKey, code, expireSeconds);

        // 格式化时间描述
        String timeDescription = business.getExpireTime() + " " +
                (business.getExpireTime() > 1 ?
                        business.getTimeUnit().name().toLowerCase() + "s" :
                        business.getTimeUnit().name().toLowerCase());

        // 获取当前时间戳（使用优化后的DateTypeUtil）
        String timestamp = DateTypeUtil.formatDateTime(ZonedDateTime.now());

        try {
            // 创建简化版邮件消息对象
            SerializableMailMessage mailMessage = new SerializableMailMessage();
            mailMessage.setTo(email);
            mailMessage.setSubject(business.getDescription());
            mailMessage.setText(buildEmailContent(code, timeDescription, timestamp));
            mailMessage.setHtml(true);
            mailMessage.setFrom(environment.getProperty("MAIL_ACCOUNT"));
            mailMessage.setSentDate(new Date());

            // 发送邮件消息到队列
            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, mailMessage);
            log.info("发送{}邮件到: {}", business.getDescription(), email);
        } catch (Exception e) {
            // 删除已生成的验证码
            redisUtil.delete(redisKey);
            log.error("发送{}邮件失败: {}", business.getDescription(), e.getMessage(), e);
            throw new ServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "邮件发送失败，请稍后重试");
        }
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void receiveEmailMessage(SerializableMailMessage mailMessage){
        try {
            // 创建 MimeMessage 并设置内容
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(mailMessage.getTo());
            helper.setSubject(mailMessage.getSubject());
            helper.setText(mailMessage.getText(), mailMessage.isHtml());
            helper.setFrom(mailMessage.getFrom());

            if (mailMessage.getSentDate() != null) {
                helper.setSentDate(mailMessage.getSentDate());
            }
            mailSender.send(mimeMessage);
            log.info("邮件发送成功: {}", mailMessage.getTo());
        } catch (MessagingException e) {
            log.error("邮件发送失败: {}", e.getMessage(), e);
            throw new ServerException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "邮件发送失败："+ e.getMessage());
        }
    }
}
