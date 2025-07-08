package com.must.courseevaluation.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncEmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    /**
     * 异步发送HTML邮件
     */
    @Async
    public CompletableFuture<Boolean> sendHtmlEmailAsync(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            mailSender.send(message);
            logger.info("异步HTML邮件发送成功至: {}", to);
            return CompletableFuture.completedFuture(true);
        } catch (MessagingException e) {
            logger.error("异步发送HTML邮件失败: {}", e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
    
    /**
     * 异步发送验证码邮件
     */
    @Async
    public CompletableFuture<Boolean> sendVerificationCodeEmailAsync(String to, String code) {
        String subject = "课程评价系统 - 邮箱验证码";
        String htmlContent = buildVerificationEmailContent(code);
        
        return sendHtmlEmailAsync(to, subject, htmlContent);
    }
    
    /**
     * 构建验证码邮件内容
     */
    private String buildVerificationEmailContent(String code) {
        return "<div style='background-color: #f4f7f9; padding: 20px; font-family: Arial, sans-serif;'>"
                + "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; padding: 30px; box-shadow: 0 3px 6px rgba(0,0,0,0.1);'>"
                + "<div style='text-align: center; margin-bottom: 30px;'>"
                + "<h1 style='color: #2c3e50; margin: 0;'>课程评价系统</h1>"
                + "<p style='color: #7f8c8d; margin: 5px 0 0 0;'>邮箱验证</p>"
                + "</div>"
                + "<h2 style='color: #2c3e50; margin-top: 0;'>邮箱验证码</h2>"
                + "<p style='color: #555; font-size: 16px;'>尊敬的用户：</p>"
                + "<p style='color: #555; font-size: 16px;'>您好！您正在注册课程评价系统账号，请使用以下验证码完成邮箱验证：</p>"
                + "<div style='background-color: #f2f4f6; padding: 20px; border-radius: 8px; text-align: center; margin: 25px 0;'>"
                + "<span style='color: #1e88e5; font-size: 32px; font-weight: bold; letter-spacing: 8px; font-family: monospace;'>" + code + "</span>"
                + "</div>"
                + "<div style='background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 5px; padding: 15px; margin: 20px 0;'>"
                + "<p style='color: #856404; margin: 0; font-size: 14px;'><strong>安全提示：</strong></p>"
                + "<ul style='color: #856404; margin: 5px 0 0 0; padding-left: 20px; font-size: 14px;'>"
                + "<li>验证码有效期为10分钟</li>"
                + "<li>请勿将验证码泄露给他人</li>"
                + "<li>如非本人操作，请忽略此邮件</li>"
                + "</ul>"
                + "</div>"
                + "<div style='margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; color: #999; font-size: 13px; text-align: center;'>"
                + "<p>这是一封自动发送的邮件，请勿直接回复。</p>"
                + "<p>© " + java.time.Year.now().getValue() + " 课程评价系统 - 保护您的学习体验</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }
}