package com.br3akPoint.recipe_service.service;

import com.br3akPoint.recipe_service.cache.RecipeCacheManager;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import response.PageData;
import util.UserContext;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRequestRepo requestRepo;
    private final RecipeRepository recipeRepository;
    private final RecipeEventPublisherService publisherService;
    private final RecipeCacheManager recipeCacheManager;

    public RecipeRequest addRecipeRequest(String content, String requestType, String cuisine) {
        RecipeRequest request = RecipeRequest.builder()
                .type(RecipeRequestType.valueOf(requestType))
                .userId(UserContext.getUserId())
                .status(RecipeStatus.processing)
                .cuisine(cuisine)
                .content(content)
                .build();

        requestRepo.save(request);

        //invalidate cache for recipe request
        recipeCacheManager.recipeRequestInvalidate(UserContext.getUserId());

        var eventBuilder = EventRecipeRequestCreated.builder()
                .requestId(request.getId())
                .userId(request.getUserId())
                .content(request.getContent())
                .type(request.getType().name());

        if(cuisine != null) {
            eventBuilder.cuisine(cuisine);
        }

        var event = eventBuilder.build();
        publisherService.publishRecipeRequestCreated(event);

        return request;
    }

    public PageData<RecipeRequest> getAllRequestsByUserId(Long userId, int page, int count) {
        return recipeCacheManager.recipeRequestGetPaged(userId, page, count).orElseGet(() -> {
            Pageable pageable = PageRequest.of(page - 1, count);
            var dbData = requestRepo.findByUserId(userId, pageable);
            var pageData = PageData.fromPageData(dbData.getContent(), dbData.getTotalPages(), dbData.getNumber(), dbData.getSize());
            recipeCacheManager.recipeRequestPutPaged(userId, page, count, pageData);
            return pageData;
        });

    }

    public Recipe createNewRecipe(SaveRecipeDTO dto) throws Exception {
        RecipeRequest request = requestRepo.findById(dto.getRequestId())
                .orElseThrow(() -> BusinessException.notFound(ServerError.Recipe_RequestId_Not_Found.getMessage()));

        Recipe recipe = RecipeMapper.toEntity(dto, request);

        recipeRepository.save(recipe);

        //invalidate recipe cache
        recipeCacheManager.recipeInvalidate(dto.getUserId());

        //update request status
        request.setStatus(RecipeStatus.completed);
        requestRepo.save(request);

        return recipe;
    }

    public PageData<Recipe> getAllByUserId(Long userId, int page, int count) {
        return recipeCacheManager.recipeGetPaged(userId, page, count).orElseGet(()-> {
            Pageable pageable = PageRequest.of(page - 1, count);
            var freshData = recipeRepository.findByUserId(userId, pageable);
            var pageData = PageData.fromPageData(freshData.getContent(), freshData.getTotalPages(), freshData.getNumber(), freshData.getSize());
            recipeCacheManager.recipePutPaged(userId, page, count, pageData);
            return pageData;
        });
    }

    public RecipeRequest updateRecipeRequest(UpdateRecipeRequestDTO dto) {
        var request = requestRepo.findByIdAndUserId(dto.getRequestId(), dto.getUserId())
                .orElseThrow(()-> BusinessException.notFound(ServerError.Recipe_RequestId_Not_Found.getMessage()));

        //update status
        request.setStatus(RecipeStatus.valueOf(dto.getStatus()));
        request.setFailReason(dto.getReason());

        requestRepo.save(request);

        //request status updated invalidate
        recipeCacheManager.recipeRequestInvalidate(dto.getUserId());

        return request;
    }

    public void removeRecipe(Long recipeId) throws Exception {
        var recipe = recipeRepository.findById(recipeId)
                .orElseThrow(()-> BusinessException.notFound(ServerError.Recipe_Not_Found.getMessage()));
        recipeRepository.delete(recipe);
        recipeCacheManager.recipeInvalidate(UserContext.getUserId());
    }

    public void removeRecipeRequest(Long requestId) throws Exception {
        //check if recipe exist w.r.t this request
        var doesRecipeExist = recipeRepository.existsByRequestId(requestId);

        if(doesRecipeExist) {
            throw BusinessException.forbidden(ServerError.Recipe_Exist_Wrt_Request_Id.getMessage());
        }

        //if not exist then directly remove
        var request = requestRepo.findById(requestId)
                .orElseThrow(()-> BusinessException.notFound(ServerError.Recipe_RequestId_Not_Found.getMessage()));

        recipeCacheManager.recipeRequestInvalidate(UserContext.getUserId());

        requestRepo.delete(request);
    }
}
