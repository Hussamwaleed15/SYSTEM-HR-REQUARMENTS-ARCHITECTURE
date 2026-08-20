package com.services.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NotificationClient {

    @Value("${services.notification.url:http://localhost:8086/api/notifications/send-email}")
    private String notificationServiceUrl;

    private final RestClient restClient;

    public NotificationClient() {
        this.restClient = RestClient.builder().build();
    }

    public boolean sendPasswordResetEmail(String toEmail, String resetCode, int expirationMinutes) {
        log.info("Sending password reset email to {} via notification service at {}", toEmail, notificationServiceUrl);
        
        String subject = "Password Reset Request - Verification Code";
        String body = "Hello,\n\n"
                + "You have requested to reset your password.\n"
                + "Your verification code is: " + resetCode + "\n\n"
                + "This code will expire in " + expirationMinutes + " minutes.\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "Best regards,\nHR System Team";

        Map<String, String> payload = new HashMap<>();
        payload.put("to", toEmail);
        payload.put("subject", subject);
        payload.put("body", body);

        try {
            restClient.post()
                    .uri(notificationServiceUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully sent password reset email request to notification service for {}", toEmail);
            return true;
        } catch (Exception e) {
            log.warn("Failed to communicate with notification service ({}). Simulated email delivery for {} with code: {}",
                    e.getMessage(), toEmail, resetCode);
            return false;
        }
    }
}
