package com.br3akPoint.ai_service.data;

import data.dto.SaveRecipeDTO;
import event.EventRecipeRequestCreated;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "recipe_processing_tracker")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipeProcessingTracker {
    private Long requestId;
    private Long userId;
    private String type;
    private String content;
    private String error;
    private String exception;
    private String status;
    private String tagId;
    private SaveRecipeDTO saveRecipeDTO;

    // --- Static Factory Methods ---

    public static RecipeProcessingTracker fromException(Exception ex, EventRecipeRequestCreated event, Long tagId) {
        RecipeProcessingTracker tracker = createBaseTracker(event, tagId);
        tracker.setException(ex.getMessage());
        tracker.setStatus("FAILED");
        return tracker;
    }

    public static RecipeProcessingTracker fromError(String error, EventRecipeRequestCreated event, Long tagId, SaveRecipeDTO saveRecipeDTO) {
        RecipeProcessingTracker tracker = createBaseTracker(event, tagId);
        tracker.setError(error);
        tracker.setStatus("FAILED");
        tracker.setSaveRecipeDTO(saveRecipeDTO);
        return tracker;
    }

    public static RecipeProcessingTracker success(EventRecipeRequestCreated event, Long tagId) {
        RecipeProcessingTracker tracker = createBaseTracker(event, tagId);
        tracker.setStatus("SUCCESS");
        return tracker;
    }

    // --- Private Helper to avoid duplication ---

    private static RecipeProcessingTracker createBaseTracker(EventRecipeRequestCreated event, Long tagId) {
        return RecipeProcessingTracker.builder()
                .requestId(event.getRequestId())
                .userId(event.getUserId())
                .type(event.getType())
                .content(event.getContent())
                .tagId(tagId.toString())
                .build();
    }
}