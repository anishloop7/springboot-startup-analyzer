package io.github.anishraj.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Handles async notifications — email, push, SMS.
 * Deliberately a lazy-load candidate: not needed at startup.
 */
@Slf4j
@Service
public class NotificationService {

    @Async
    public void sendOrderConfirmation(String email, Long orderId) {
        // In production: integrate with SendGrid, SES, etc.
        log.info("Sending order confirmation to {} for order #{}", email, orderId);
    }

    @Async
    public void sendWelcomeEmail(String email, String username) {
        log.info("Sending welcome email to {}", email);
    }

    @Async
    public void sendPasswordResetEmail(String email, String resetToken) {
        log.info("Sending password reset email to {}", email);
    }

    @Async
    public void sendAdminAlert(String message) {
        log.warn("Admin alert: {}", message);
    }
}
