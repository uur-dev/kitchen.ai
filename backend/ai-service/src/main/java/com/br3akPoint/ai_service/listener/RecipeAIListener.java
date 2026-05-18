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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import service.RedisService;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class RecipeAIListener {

    private static final String MSG_FAILED    = "An error has occurred while processing your recipe.";
    private static final String MSG_COMPLETED = "Your Recipe is ready and complete.";
    private static final String CACHE_KEY_PREFIX  = "recipe-ai-response:request-id:";
    private static final Duration CACHE_TTL       = Duration.ofMinutes(8);

    private final RecipeAIService recipeAIService;
    private final RecipeClient recipeClient;
    private final RecipeTrackerService trackerService;
    private final RabbitTemplate rabbitTemplate;
    private final RedisService redisService;

    // -------------------------------------------------------------------------
    // Entry Point
    // -------------------------------------------------------------------------

    @RabbitListener(queues = MessageBrokerKeys.RECIPE_QUEUE_NAME)
    public void processRecipeRequest(
            EventRecipeRequestCreated request,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        log.info("AI Service processing recipe requestId={}", request.getRequestId());
        try {
            RecipeAIResponse response = resolveAIResponse(request);
            handleAIResponse(response, request, tag);
            log.info("Successfully processed recipe requestId={}", request.getRequestId());
        } catch (Exception e) {
            log.error("Error processing recipe requestId={}", request.getRequestId(), e);
            handleException(e, request, tag);
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    // AI Response Resolution (cache-aside pattern)
    // -------------------------------------------------------------------------

    /**
     * Returns a cached AI response if one exists, otherwise calls the AI service
     * and caches the result for retry resilience.
     */
    private RecipeAIResponse resolveAIResponse(EventRecipeRequestCreated event) throws Exception {
        String cacheKey = buildCacheKey(event.getRequestId());

        return redisService.get(cacheKey)
                .map(cached -> {
                    RecipeAIResponse response = (RecipeAIResponse) cached;
                    log.info("Cache hit for requestId={}, title={}", event.getRequestId(), response.getTitle());
                    return response;
                })
                .orElseGet(() -> {
                    try {
                        return fetchAndCacheAIResponse(event, cacheKey);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private RecipeAIResponse fetchAndCacheAIResponse(EventRecipeRequestCreated event, String cacheKey) throws IOException {
        RecipeAIResponse response = callAIService(event);
        redisService.set(cacheKey, response, CACHE_TTL);
        log.info("Fetched and cached AI recipe title={} for requestId={}", response.getTitle(), event.getRequestId());
        return response;
    }

    private RecipeAIResponse callAIService(EventRecipeRequestCreated event) throws IOException {
        boolean isTextRequest = "text".equalsIgnoreCase(event.getType());
        return isTextRequest
                ? recipeAIService.getRecipeByText(event.getContent(), event.getCuisine())
                : recipeAIService.getRecipeByImageOrAudio(new UrlMultipartFile(event.getContent()), event.getCuisine());
    }

    // -------------------------------------------------------------------------
    // AI Response Handling
    // -------------------------------------------------------------------------

    private void handleAIResponse(RecipeAIResponse response, EventRecipeRequestCreated event, long tag) {
        if (Boolean.TRUE.equals(response.getStatus())) {
            saveRecipe(response, event, tag);
        } else if (StringUtils.hasText(response.getReason())) {
            log.error("AI rejected recipe requestId={}, reason={}", event.getRequestId(), response.getReason());
            handleError(response.getReason(), event, tag, null);
        } else {
            log.warn("AI returned unknown failure state for requestId={}", event.getRequestId());
            handleError(MSG_FAILED, event, tag, null);
        }
    }

    // -------------------------------------------------------------------------
    // Recipe Persistence
    // -------------------------------------------------------------------------

    private void saveRecipe(RecipeAIResponse response, EventRecipeRequestCreated event, long tag) {
        SaveRecipeDTO dto = buildSaveRecipeDTO(response, event);
        var serviceResponse = recipeClient.saveRecipe(dto);

        if (Boolean.TRUE.equals(serviceResponse.getStatus())) {
            log.info("Saved recipe title={} for requestId={}", dto.getTitle(), event.getRequestId());
            evictCache(event.getRequestId());
            handleSuccess(event, tag, dto);
        } else {
            String error = "error_key:recipe-internal-service-failed";
            log.error("Recipe service save failed for requestId={}", event.getRequestId());
            handleError(error, event, tag, dto);
        }
    }

    private SaveRecipeDTO buildSaveRecipeDTO(RecipeAIResponse response, EventRecipeRequestCreated event) {
        List<String> ingredientNames = response.getIngredients().stream()
                .map(i -> i.getName().toLowerCase())
                .toList();

        List<Map<String, Object>> ingredients = response.getIngredients().stream()
                .map(RecipeAIResponse.Ingredient::getMap)
                .toList();

        List<Map<String, Object>> steps = response.getSteps().stream()
                .map(RecipeAIResponse.Step::getMap)
                .toList();

        return SaveRecipeDTO.builder()
                .userId(event.getUserId())
                .requestId(event.getRequestId())
                .title(response.getTitle())
                .image(response.getImage())
                .description(response.getDescription())
                .servings(response.getServings())
                .cuisine(response.getCuisine())
                .difficulty(response.getDifficulty())
                .cookTimeMins(response.getCookTimeMinutes())
                .prepTimeMins(response.getPrepTimeMinutes())
                .tags(response.getTags())
                .requestType(event.getType())
                .ingredientsList(ingredientNames)
                .steps(steps)
                .ingredients(ingredients)
                .nutritionInfo(response.getNutritionInfo().getMap())
                .build();
    }

    // -------------------------------------------------------------------------
    // Tracking & Notifications
    // -------------------------------------------------------------------------

    private void handleSuccess(EventRecipeRequestCreated event, long tag, SaveRecipeDTO dto) {
        trackerService.trackSuccess(event, tag);
        sendNotification(event.getUserId(), event.getRequestId(),
                RecipeStatus.completed.name(), MSG_COMPLETED, dto.toMap());
    }

    private void handleError(String reason, EventRecipeRequestCreated event, long tag, SaveRecipeDTO dto) {
        markRequestFailed(event, reason);
        trackerService.trackError(reason, event, tag, dto);
        sendNotification(event.getUserId(), event.getRequestId(),
                RecipeStatus.failed.name(), MSG_FAILED, Map.of("error", reason));
    }

    private void handleException(Exception exception, EventRecipeRequestCreated event, long tag) {
        markRequestFailed(event, MSG_FAILED);
        trackerService.trackException(exception, event, tag);
        sendNotification(event.getUserId(), event.getRequestId(),
                RecipeStatus.failed.name(), MSG_FAILED, Map.of("exception", exception.getMessage()));
    }

    // -------------------------------------------------------------------------
    // Infrastructure Helpers
    // -------------------------------------------------------------------------

    private void markRequestFailed(EventRecipeRequestCreated event, String reason) {
        try {
            var dto = UpdateRecipeRequestDTO.builder()
                    .userId(event.getUserId())
                    .requestId(event.getRequestId())
                    .status("failed")
                    .reason(reason)
                    .build();

            boolean updated = Boolean.TRUE.equals(recipeClient.updateRecipeRequest(dto).getStatus());
            if (updated) {
                log.info("Marked request as failed for requestId={}", event.getRequestId());
            } else {
                log.error("Failed to update status for requestId={}", event.getRequestId());
            }
        } catch (Exception e) {
            log.error("Could not mark request as failed for requestId={}", event.getRequestId(), e);
        }
    }

    private void sendNotification(Long userId, Long requestId, String status, String summary, Map<String, Object> result) {
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
            log.error("Failed to send notification for requestId={}", requestId, e);
        }
    }

    private void evictCache(Long requestId) {
        redisService.delete(buildCacheKey(requestId));
    }

    private String buildCacheKey(Long requestId) {
        return CACHE_KEY_PREFIX + requestId;
    }
}