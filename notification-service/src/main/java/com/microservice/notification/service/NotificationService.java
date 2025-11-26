package com.microservice.notification.service;

import com.microservice.notification.dto.ClaimCreatedEvent;
import com.microservice.notification.dto.ClaimStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    //TODO: Enviar mail usando JavaMailSender(?
    public void sendClaimCreatedNotification(ClaimCreatedEvent event) {
        log.info("📧 NOTIFICATION: New claim created!");
        log.info("   Claim ID: {}", event.getClaimId());
        log.info("   Policy ID: {}", event.getPolicyId());
        log.info("   Amount: ${}", event.getAmount());
        log.info("   Status: {}", event.getStatus());
        log.info("   ✉️ Email sent to policy holder");
        log.info("   📱 SMS sent to policy holder");
    }

    public void sendClaimStatusChangedNotification(ClaimStatusChangedEvent event) {
        log.info("📧 NOTIFICATION: Claim status changed!");
        log.info("   Claim ID: {}", event.getClaimId());
        log.info("   Old Status: {}", event.getOldStatus());
        log.info("   New Status: {}", event.getNewStatus());
        log.info("   ✉️ Email sent to policy holder");
        log.info("   📱 SMS sent to policy holder");
    }
}