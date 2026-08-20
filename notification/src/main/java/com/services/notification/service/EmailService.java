package com.services.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:dummy@gmail.com}")
    private String fromEmail;

    public boolean sendEmail(String to, String subject, String body) {
        log.info("Preparing to send email to: '{}' with subject: '{}'", to, subject);

        if (mailSender == null || "dummy@gmail.com".equalsIgnoreCase(fromEmail) || "dummypassword".equals(fromEmail)) {
            log.info("[MOCK/DEV MODE] Email simulated successfully to: {} | Subject: {}", to, subject);
            return true;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject != null ? subject : "HR Recruitment Notification");
            message.setText(body != null ? body : "");

            mailSender.send(message);
            log.info("Email successfully sent via SMTP to: {}", to);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            log.info("[FALLBACK] Email logged to console: To: {} | Subject: {} | Body: {}", to, subject, body);
            return false;
        }
    }

    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        log.info("Preparing to send HTML email to: '{}' with subject: '{}'", to, subject);

        if (mailSender == null || "dummy@gmail.com".equalsIgnoreCase(fromEmail)) {
            log.info("[MOCK/DEV MODE] HTML Email simulated successfully to: {} | Subject: {}", to, subject);
            return true;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject != null ? subject : "HR Recruitment Notification");
            helper.setText(htmlContent != null ? htmlContent : "", true);

            mailSender.send(message);
            log.info("HTML Email successfully sent via SMTP to: {}", to);
            return true;
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            log.info("[FALLBACK] HTML Email logged to console: To: {} | Subject: {}", to, subject);
            return false;
        }
    }
}