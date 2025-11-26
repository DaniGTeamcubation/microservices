package com.microservice.notification.listener;

import com.microservice.notification.config.RabbitMQConfig;
import com.microservice.notification.dto.ClaimCreatedEvent;
import com.microservice.notification.dto.ClaimStatusChangedEvent;
import com.microservice.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClaimEventListener {
    private final NotificationService notificationService;

    public ClaimEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.CLAIM_CREATED_QUEUE)
    public void handleClaimCreated(ClaimCreatedEvent event) {
        log.info("Received ClaimCreatedEvent: {}", event);
        notificationService.sendClaimCreatedNotification(event);
    }

    @RabbitListener(queues = RabbitMQConfig.CLAIM_STATUS_CHANGED_QUEUE)
    public void handleClaimStatusChanged(ClaimStatusChangedEvent event) {
        log.info("Received ClaimStatusChangedEvent: {}", event);
        notificationService.sendClaimStatusChangedNotification(event);
    }
}