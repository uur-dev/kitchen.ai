package com.br3akPoint.recipe_service.config;

import constant.MessageBrokerKeys;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public TopicExchange recipeExchange() {
        return new TopicExchange(MessageBrokerKeys.RECIPE_EXCHANGE_NAME);
    }

    @Bean
    public Queue recipeQueue() {
       /* return new Queue(RecipeMessageBrokerKeys.RECIPE_QUEUE_NAME, true);*/
        return QueueBuilder
                .durable(MessageBrokerKeys.RECIPE_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange",
                        MessageBrokerKeys.RECIPE_DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key",
                        MessageBrokerKeys.RECIPE_DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding recipeBinding() {
        return BindingBuilder
                .bind(recipeQueue())
                .to(recipeExchange())
                .with(MessageBrokerKeys.RECIPE_ROUTING_KEY);
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder
                .bind(recipeDlxQueue())
                .to(recipeDlxExchange())
                .with(MessageBrokerKeys.RECIPE_DLX_ROUTING_KEY);
    }

    /// - DLX (DLQ)
    @Bean
    public Queue recipeDlxQueue() {
        return QueueBuilder
                .durable(MessageBrokerKeys.RECIPE_DLX_QUEUE)
                .build();
    }

    @Bean
    public TopicExchange recipeDlxExchange() {
        return new TopicExchange(MessageBrokerKeys.RECIPE_DLX_EXCHANGE);
    }
}
