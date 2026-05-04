package com.br3akPoint.ai_service.listener;

import com.br3akPoint.ai_service.data.RecipeAIResponse;
import com.br3akPoint.ai_service.service.RecipeAIService;
import com.br3akPoint.ai_service.util.UrlMultipartFile;
import constant.RecipeMessageBrokerKeys;
import event.EventRecipeRequestCreated;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RecipeAIListener {

    private final RecipeAIService recipeAIService;

    public RecipeAIListener(RecipeAIService recipeAIService) {
        this.recipeAIService = recipeAIService;
    }

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
        RecipeAIResponse response = null;
        if(event.getType().equals("text")) {
            response = recipeAIService.getRecipeByText(event.getContent());
        } else {
            var multiPartFile = new UrlMultipartFile(event.getContent());
            response = recipeAIService.getRecipeByImageOrAudio(multiPartFile);
        }

        if(response == null) { return;}

        if(response.getStatus()) {
            log.info("Successfully got AI recipe: {}", response.getTitle());
        } else if(response.getReason() != null && !response.getReason().isBlank()) {
            log.info("Error In AI recipe: {}", response.getReason());
        }
    }

}
