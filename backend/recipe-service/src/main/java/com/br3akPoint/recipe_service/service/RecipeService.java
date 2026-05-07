package com.br3akPoint.recipe_service.service;

import com.br3akPoint.recipe_service.constant.RecipeRequestType;
import com.br3akPoint.recipe_service.constant.RecipeStatus;
import com.br3akPoint.recipe_service.constant.ServerError;
import com.br3akPoint.recipe_service.data.mapper.RecipeMapper;
import com.br3akPoint.recipe_service.entity.Recipe;
import com.br3akPoint.recipe_service.entity.RecipeRequest;
import com.br3akPoint.recipe_service.repository.RecipeRepository;
import com.br3akPoint.recipe_service.repository.RecipeRequestRepo;
import data.dto.SaveRecipeDTO;
import data.dto.UpdateRecipeRequestDTO;
import error.BusinessException;
import event.EventRecipeRequestCreated;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import util.UserContext;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRequestRepo requestRepo;
    private final RecipeRepository recipeRepository;
    private final RecipeEventPublisherService publisherService;

    public RecipeRequest addRecipeRequest(String content, String requestType) {
        RecipeRequest request = RecipeRequest.builder()
                .type(RecipeRequestType.valueOf(requestType))
                .userId(UserContext.getUserId())
                .status(RecipeStatus.processing)
                .content(content)
                .build();

        requestRepo.save(request);

        publisherService.publishRecipeRequestCreated(EventRecipeRequestCreated.builder()
                        .requestId(request.getId())
                        .userId(request.getUserId())
                        .content(request.getContent())
                        .type(request.getType().name())
                .build());

        return request;
    }

    public List<RecipeRequest> getAllRequestsByUserId(Long userId, int page, int count) {
        Pageable pageable = PageRequest.of(page - 1, count);
        var result = requestRepo.findByUserId(userId, pageable);
        return result.getContent();
    }

    public Recipe createNewRecipe(SaveRecipeDTO dto) throws Exception {
        RecipeRequest request = requestRepo.findById(dto.getRequestId())
                .orElseThrow(() -> BusinessException.notFound(ServerError.Recipe_RequestId_Not_Found.getMessage()));

        Recipe recipe = RecipeMapper.toEntity(dto, request);

        recipeRepository.save(recipe);

        //update request status
        request.setStatus(RecipeStatus.completed);
        requestRepo.save(request);

        return recipe;
    }

    public List<Recipe> getAllByUserId(Long userId, int page, int count) {
        Pageable pageable = PageRequest.of(page - 1, count);
        var result = recipeRepository.findByUserId(userId, pageable);
        return result.getContent();
    }

    public RecipeRequest updateRecipeRequest(UpdateRecipeRequestDTO dto) {
        var request = requestRepo.findByIdAndUserId(dto.getRequestId(), dto.getUserId())
                .orElseThrow(()-> BusinessException.notFound(ServerError.Recipe_RequestId_Not_Found.getMessage()));

        //update status
        request.setStatus(RecipeStatus.valueOf(dto.getStatus()));
        request.setFailReason(dto.getReason());

        requestRepo.save(request);

        return request;
    }
}
