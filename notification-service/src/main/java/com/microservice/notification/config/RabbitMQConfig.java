package com.microservice.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "claims-exchange";
    public static final String CLAIM_CREATED_QUEUE = "claim-created-queue";
    public static final String CLAIM_STATUS_CHANGED_QUEUE = "claim-status-changed-queue";
    public static final String CLAIM_CREATED_ROUTING_KEY = "claim.created";
    public static final String CLAIM_STATUS_CHANGED_ROUTING_KEY = "claim.status.changed";

    @Bean
    public TopicExchange claimsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue claimCreatedQueue() {
        return new Queue(CLAIM_CREATED_QUEUE, true);
    }

    @Bean
    public Queue claimStatusChangedQueue() {
        return new Queue(CLAIM_STATUS_CHANGED_QUEUE, true);
    }

    @Bean
    public Binding claimCreatedBinding() {
        return BindingBuilder
                .bind(claimCreatedQueue())
                .to(claimsExchange())
                .with(CLAIM_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding claimStatusChangedBinding() {
        return BindingBuilder
                .bind(claimStatusChangedQueue())
                .to(claimsExchange())
                .with(CLAIM_STATUS_CHANGED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}