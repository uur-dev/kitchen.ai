package com.br3akPoint.recipe_service.config;

import constant.RecipeMessageBrokerKeys;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public TopicExchange recipeExchange() {
        return new TopicExchange(RecipeMessageBrokerKeys.RECIPE_EXCHANGE_NAME);
    }

    @Bean
    public Queue recipeQueue() {
       /* return new Queue(RecipeMessageBrokerKeys.RECIPE_QUEUE_NAME, true);*/
        return QueueBuilder
                .durable(RecipeMessageBrokerKeys.RECIPE_QUEUE_NAME)
                .withArgument("x-dead-letter-exchange",
                        RecipeMessageBrokerKeys.DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key",
                        RecipeMessageBrokerKeys.DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding recipeBinding() {
        return BindingBuilder
                .bind(recipeQueue())
                .to(recipeExchange())
                .with(RecipeMessageBrokerKeys.RECIPE_ROUTING_KEY);
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder
                .bind(recipeDlxQueue())
                .to(recipeDlxExchange())
                .with(RecipeMessageBrokerKeys.DLX_ROUTING_KEY);
    }

    /// - DLX (DLQ)
    @Bean
    public Queue recipeDlxQueue() {
        return QueueBuilder
                .durable(RecipeMessageBrokerKeys.DLX_QUEUE)
                .build();
    }

    @Bean
    public TopicExchange recipeDlxExchange() {
        return new TopicExchange(RecipeMessageBrokerKeys.DLX_EXCHANGE);
    }
}
