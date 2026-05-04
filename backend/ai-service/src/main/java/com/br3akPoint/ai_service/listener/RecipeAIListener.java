package com.br3akPoint.ai_service.listener;

import com.br3akPoint.ai_service.client.RecipeClient;
import com.br3akPoint.ai_service.data.RecipeAIResponse;
import com.br3akPoint.ai_service.service.RecipeAIService;
import com.br3akPoint.ai_service.util.UrlMultipartFile;
import constant.RecipeMessageBrokerKeys;
import data.dto.SaveRecipeDTO;
import event.EventRecipeRequestCreated;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class RecipeAIListener {

    private final RecipeAIService recipeAIService;
    private final RecipeClient recipeClient;

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
            saveToRecipe(event, response);
        } else if(response.getReason() != null && !response.getReason().isBlank()) {
            log.info("Error In AI recipe: {}", response.getReason());
        }
    }

    private void saveToRecipe(EventRecipeRequestCreated event, RecipeAIResponse response) {
        List<String> ingredientList = response.getIngredients().stream()
                .map(RecipeAIResponse.Ingredient::getName)
                .map(String::toLowerCase)
                .toList();

        List<Map<String, Object>> steps = response.getSteps().stream()
                .map(RecipeAIResponse.Step::getMap)
                .toList();

        List<Map<String, Object>> ingredients = response.getIngredients().stream()
                .map(RecipeAIResponse.Ingredient::getMap)
                .toList();

        Map<String, Object> nutritionInfo = response.getNutritionInfo().getMap();

        SaveRecipeDTO dto = SaveRecipeDTO.builder()
                .userId(event.getUserId())
                .requestId(event.getRequestId())
                .title(response.getTitle())
                .description(response.getDescription())
                .servings(response.getServings())
                .cuisine(response.getCuisine())
                .difficulty(response.getDifficulty())
                .cookTimeMins(response.getCookTimeMinutes())
                .prepTimeMins(response.getPrepTimeMinutes())
                .tags(response.getTags())
                .requestType(event.getType())
                .ingredientsList(ingredientList)
                .steps(steps)
                .ingredients(ingredients)
                .nutritionInfo(nutritionInfo)
                .build();

        var recipeServiceResponse = recipeClient.saveRecipe(dto);
        log.info("Successfully Saved recipe: {}", recipeServiceResponse.getStatus());
    }

}
