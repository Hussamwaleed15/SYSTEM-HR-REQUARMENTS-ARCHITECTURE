package com.services.notification.controller;

import com.services.notification.dto.EmailRequest;
import com.services.notification.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/send-email")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody EmailRequest request) {
        boolean sent = emailService.sendEmail(request.getTo(), request.getSubject(), request.getBody());

        Map<String, Object> response = new HashMap<>();
        response.put("message", sent ? "Email notification sent successfully" : "Email notification logged (SMTP not configured or failed)");
        response.put("to", request.getTo());
        response.put("subject", request.getSubject());
        response.put("success", sent);

        return ResponseEntity.ok(response);
    }
}