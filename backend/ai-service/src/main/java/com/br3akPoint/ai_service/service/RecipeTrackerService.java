package com.br3akPoint.ai_service.service;

import com.br3akPoint.ai_service.data.RecipeProcessingTracker;
import com.br3akPoint.ai_service.repository.TrackerRepository;
import data.dto.SaveRecipeDTO;
import event.EventRecipeRequestCreated;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RecipeTrackerService {
    private final TrackerRepository trackerRepository;

    public void trackException(Exception ex, EventRecipeRequestCreated event, Long tag) {
        var trackObject = RecipeProcessingTracker.fromException(ex, event, tag);
        trackerRepository.save(trackObject);
    }

    public void trackError(String error, EventRecipeRequestCreated event, Long tag, SaveRecipeDTO dto) {
        var trackObject = RecipeProcessingTracker.fromError(error, event, tag, dto);
        trackerRepository.save(trackObject);
    }

    public void trackSuccess(EventRecipeRequestCreated event, Long tag) {
        var trackObject = RecipeProcessingTracker.success(event, tag);
        trackerRepository.save(trackObject);
    }
}
