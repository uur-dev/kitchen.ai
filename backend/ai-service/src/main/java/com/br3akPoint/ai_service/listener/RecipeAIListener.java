package com.br3akPoint.ai_service.listener;

import com.rabbitmq.client.Channel;
import constant.RecipeMessageBrokerKeys;
import event.EventRecipeRequestCreated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RecipeAIListener {

    @RabbitListener(queues = RecipeMessageBrokerKeys.RECIPE_QUEUE_NAME)
    public void processRecipeRequest(
            EventRecipeRequestCreated request/*,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag*/) throws Exception{
        log.info("AI Service processing recipe: {}", request.getRequestId());

        try {
            // Your GenAI logic here
            // If something goes wrong, throw an exception to trigger Retry
            processRecipeWithGenAI(request);

            log.info("Successfully processed recipe: {}", request.getRequestId());

            // Success: Manual ACK
           // channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Error processing recipe {}. Retrying...", request.getRequestId());
            throw e; // Throwing exception triggers the Spring Retry mechanism
        }
    }

    private void processRecipeWithGenAI(EventRecipeRequestCreated event) throws Exception {
        // TODO: Integrate your Google GenAI SDK logic here
        // Example: generateRecipeDetails(event.rawIngredients());
        throw new Exception("testing");
    }

}
