package com.microservice.publisher;

import com.microservice.config.RabbitMQConfig;
import com.microservice.dto.ClaimCreatedEvent;
import com.microservice.dto.ClaimStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClaimEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public ClaimEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishClaimCreated(ClaimCreatedEvent event) {
        log.info("Publishing ClaimCreatedEvent: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.CLAIM_CREATED_ROUTING_KEY,
                event
        );
    }

    public void publishClaimStatusChanged(ClaimStatusChangedEvent event) {
        log.info("Publishing ClaimStatusChangedEvent: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.CLAIM_STATUS_CHANGED_ROUTING_KEY,
                event
        );
    }
}