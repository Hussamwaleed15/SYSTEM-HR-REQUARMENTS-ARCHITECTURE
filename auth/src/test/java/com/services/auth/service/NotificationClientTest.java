package com.services.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationClientTest {

    @Test
    @DisplayName("NotificationClient fallback when notification service is not reachable")
    void sendPasswordResetEmail_GracefulFallback() {
        NotificationClient notificationClient = new NotificationClient();
        // Since notification service isn't running on port 8086 during unit tests, it should gracefully return false and not throw an exception
        boolean result = notificationClient.sendPasswordResetEmail("test@example.com", "123456", 15);
        assertThat(result).isFalse();
    }
}
