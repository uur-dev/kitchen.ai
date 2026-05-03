package com.br3akPoint.ai_service.config;

import constant.RecipeMessageBrokerKeys;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Queue aiQueue() {
        return QueueBuilder.durable(RecipeMessageBrokerKeys.RECIPE_QUEUE_NAME)
                .build();
    }
}
