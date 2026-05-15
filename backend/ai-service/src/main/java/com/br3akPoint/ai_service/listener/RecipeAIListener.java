package com.br3akPoint.ai_service.listener;

import com.br3akPoint.ai_service.client.RecipeClient;
import com.br3akPoint.ai_service.data.RecipeAIResponse;
import com.br3akPoint.ai_service.service.RecipeAIService;
import com.br3akPoint.ai_service.service.RecipeTrackerService;
import com.br3akPoint.ai_service.util.UrlMultipartFile;
import constant.MessageBrokerKeys;
import constant.RecipeStatus;
import data.dto.SaveRecipeDTO;
import data.dto.UpdateRecipeRequestDTO;
import event.EventRecipeProcessCompleted;
import event.EventRecipeRequestCreated;
import data.dto.RecipeResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class RecipeAIListener {

    private static final String MSG_FAILED       = "An error has occurred while processing your recipe.";
    private static final String MSG_COMPLETED    = "Your Recipe is ready and complete.";

    private final RecipeAIService recipeAIService;
    private final RecipeClient recipeClient;
    private final RecipeTrackerService trackerService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = MessageBrokerKeys.RECIPE_QUEUE_NAME)
    public void processRecipeRequest(
            EventRecipeRequestCreated request,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        log.info("AI Service processing recipe requestId={}", request.getRequestId());
        try {
            processRecipeWithGenAI(request, tag);
            log.info("Successfully processed recipe requestId={}", request.getRequestId());
        } catch (Exception e) {
            log.error("Error processing recipe requestId={}. Retrying...", request.getRequestId());
            trackException(e, request, tag);
            throw e;
        }
    }

    private void processRecipeWithGenAI(EventRecipeRequestCreated event, Long tagId) throws Exception {
        RecipeAIResponse response = "text".equalsIgnoreCase(event.getType())
                ? recipeAIService.getRecipeByText(event.getContent())
                : recipeAIService.getRecipeByImageOrAudio(new UrlMultipartFile(event.getContent()));

        if (response == null) return;

        if (Boolean.TRUE.equals(response.getStatus())) {
            log.info("Successfully got AI recipe: {}", response.getTitle());
            saveToRecipe(event, response, tagId);
        } else if (StringUtils.hasText(response.getReason())) {
            log.error("Error in AI recipe: {}", response.getReason());
            trackError(response.getReason(), event, tagId, null);
        }
    }

    private void saveToRecipe(EventRecipeRequestCreated event, RecipeAIResponse response, Long tag) {
        var ingredientsParsed = response.getIngredients();

        List<String> ingredientList = ingredientsParsed.stream()
                .map(i -> i.getName().toLowerCase())
                .toList();

        List<Map<String, Object>> ingredients = ingredientsParsed.stream()
                .map(RecipeAIResponse.Ingredient::getMap)
                .toList();

        List<Map<String, Object>> steps = response.getSteps().stream()
                .map(RecipeAIResponse.Step::getMap)
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

        if (recipeServiceResponse.getStatus()) {
            log.info("Successfully saved recipe: {}", dto.getTitle());
            trackSuccess(event, tag, dto);
        } else {
            String error = "error_key:recipe-internal-service-failed";
            log.error("Error saving recipe: {}", error);
            trackError(error, event, tag, dto);
        }
    }

    private void triggerNotificationEvent(Long userId, Long requestId, String status, String summary, Map<String, Object> result) {
        try {
            rabbitTemplate.convertAndSend(
                    MessageBrokerKeys.NOTIFICATION_EXCHANGE_NAME,
                    MessageBrokerKeys.NOTIFICATION_ROUTING_KEY,
                    EventRecipeProcessCompleted.builder()
                            .userId(userId)
                            .requestId(requestId)
                            .status(status)
                            .summary(summary)
                            .result(result)
                            .build());
        } catch (Exception e) {
            log.error("Exception while triggering notification event: {}", e.getMessage());
        }
    }

    private void updateRecipeFailStatus(EventRecipeRequestCreated event, String reason) {
        try {
            var dto = UpdateRecipeRequestDTO.builder()
                    .userId(event.getUserId())
                    .requestId(event.getRequestId())
                    .status("failed")
                    .reason(reason)
                    .build();

            var response = recipeClient.updateRecipeRequest(dto);

            if(response.getStatus()) {
                log.info("Recipe Request Status Updated for requestId={}", event.getRequestId());
            } else {
                log.error("Recipe Request Status Updated Error for requestId={}", event.getRequestId());
            }
        }catch (Exception e) {
            log.error("Recipe Request Status Updated Failed for requestId={}, Exception={}", event.getRequestId(), e.getMessage());
        }
    }

    private void trackError(String error, EventRecipeRequestCreated event, Long tag, SaveRecipeDTO dto) {
        updateRecipeFailStatus(event, error);
        trackerService.trackError(error, event, tag, dto);
        triggerNotificationEvent(event.getUserId(), event.getRequestId(), RecipeStatus.failed.name(), MSG_FAILED, Map.of("error", error));
    }

    private void trackException(Exception exception, EventRecipeRequestCreated event, Long tag) {
        updateRecipeFailStatus(event, MSG_FAILED);
        trackerService.trackException(exception, event, tag);
        triggerNotificationEvent(event.getUserId(), event.getRequestId(), RecipeStatus.failed.name(), MSG_FAILED, Map.of("exception", exception.getMessage()));
    }

    private void trackSuccess(EventRecipeRequestCreated event, Long tag, SaveRecipeDTO dto) {
        trackerService.trackSuccess(event, tag);
        triggerNotificationEvent(event.getUserId(), event.getRequestId(), RecipeStatus.completed.name(), MSG_COMPLETED, dto.toMap());
    }
}