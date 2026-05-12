package com.br3akPoint.recipe_service.service;

import constant.MessageBrokerKeys;
import event.EventRecipeRequestCreated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeEventPublisherService {
    private final RabbitTemplate rabbitTemplate;

    public void publishRecipeRequestCreated(EventRecipeRequestCreated event) {
        log.info("Sending event to AI Service for recipe request ID: {}", event.getRequestId());
        try {
            CorrelationData correlationData = new CorrelationData(
                    event.getRequestId().toString()
            );

            rabbitTemplate.convertAndSend(
                    MessageBrokerKeys.RECIPE_EXCHANGE_NAME,
                    MessageBrokerKeys.RECIPE_ROUTING_KEY,
                    event,
                    correlationData
            );
        } catch (Exception e) {
            log.error("Failed to publish event for recipe request ID: {}", event.getRequestId(), e);
        }
    }
}
