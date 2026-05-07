package com.br3akPoint.ai_service.config;

import constant.MessageBrokerKeys;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(MessageBrokerKeys.NOTIFICATION_EXCHANGE_NAME);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder
                .durable(MessageBrokerKeys.NOTIFICATION_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange",
                        MessageBrokerKeys.NOTIFICATION_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key",
                        MessageBrokerKeys.NOTIFICATION_DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(MessageBrokerKeys.NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder
                .bind(dlxQueue())
                .to(dlxExchange())
                .with(MessageBrokerKeys.NOTIFICATION_DLX_ROUTING_KEY);
    }

    /// - DLX (DLQ)
    @Bean
    public Queue dlxQueue() {
        return QueueBuilder
                .durable(MessageBrokerKeys.NOTIFICATION_DLX_QUEUE)
                .build();
    }

    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange(MessageBrokerKeys.NOTIFICATION_DLX_EXCHANGE);
    }
}
