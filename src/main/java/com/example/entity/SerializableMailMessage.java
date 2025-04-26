package com.example.entity;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.mail.MailMessage;
import org.springframework.mail.MailParseException;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class SerializableMailMessage implements MailMessage, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // Getter 方法
    private String from;       // 发件人
    private String to;         // 单个收件人（简化为字符串）
    private String subject;    // 主题
    private String text;       // 内容
    private boolean html;      // 是否为 HTML 格式
    private Date sentDate;     // 发送日期

    // 实现 MailMessage 接口的方法
    @Override
    public void setFrom(@NotNull String from) throws MailParseException {
        this.from = from;
    }

    @Override
    public void setTo(@NotNull String to) throws MailParseException {
        this.to = to;
    }

    @Override
    public void setTo(String... to) throws MailParseException {
        if (to.length > 0) {
            this.to = to[0]; // 只取第一个收件人
        }
    }

    @Override
    public void setSubject(@NotNull String subject) throws MailParseException {
        this.subject = subject;
    }

    @Override
    public void setText(@NotNull String text) throws MailParseException {
        this.text = text;
    }

    // 不使用的方法（可选实现）
    @Override
    public void setReplyTo(@NotNull String replyTo) throws MailParseException {
        // 无需回复地址
    }

    @Override
    public void setCc(@NotNull String cc) throws MailParseException {
        // 不支持抄送
    }

    @Override
    public void setCc(String @NotNull ... cc) throws MailParseException {
        // 不支持抄送
    }

    @Override
    public void setBcc(@NotNull String bcc) throws MailParseException {
        // 不支持密送
    }

    @Override
    public void setBcc(String @NotNull ... bcc) throws MailParseException {
        // 不支持密送
    }

    @Override
    public void setSentDate(@NotNull Date sentDate) throws MailParseException {
        this.sentDate = sentDate;
    }

}