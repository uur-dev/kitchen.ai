package com.br3akPoint.recipe_service.service;

import com.br3akPoint.error.BusinessException;
import com.br3akPoint.recipe_service.constant.RecipeRequestType;
import com.br3akPoint.recipe_service.constant.RecipeStatus;
import com.br3akPoint.recipe_service.constant.ServerError;
import com.br3akPoint.recipe_service.data.dto.request.CreateRecipeRequestDTO;
import com.br3akPoint.recipe_service.entity.Recipe;
import com.br3akPoint.recipe_service.entity.RecipeRequest;
import com.br3akPoint.recipe_service.repository.RecipeRepository;
import com.br3akPoint.recipe_service.repository.RecipeRequestRepo;
import com.br3akPoint.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeService {
    private final RecipeRequestRepo requestRepo;
    private final RecipeRepository recipeRepository;

    @Autowired
    public RecipeService(RecipeRequestRepo requestRepo, RecipeRepository recipeRepository) {
        this.requestRepo = requestRepo;
        this.recipeRepository = recipeRepository;
    }

    public RecipeRequest addRecipeRequest(String content, String requestType) {
        RecipeRequest request = RecipeRequest.builder()
                .type(RecipeRequestType.valueOf(requestType))
                .userId(UserContext.getUserId())
                .status(RecipeStatus.processing)
                .content(content)
                .build();

        requestRepo.save(request);

        return request;
    }

    public List<RecipeRequest> getAllByUserId(Long userId, int page, int count) {
        Pageable pageable = PageRequest.of(page - 1, count);
        var result = requestRepo.findByUserId(userId, pageable);
        return result.getContent();
    }

    public Recipe createNewRecipe(CreateRecipeRequestDTO dto) throws Exception {
        RecipeRequest request = requestRepo.findById(dto.getRequestId())
                .orElseThrow(() -> BusinessException.notFound(ServerError.Recipe_RequestId_Not_Found.getMessage()));

        Recipe recipe = Recipe.builder()
                .userId(dto.getUserId())
                .request(request)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .duration(dto.getDuration())
                .requestType(RecipeRequestType.valueOf(dto.getRequestType()))
                .steps(dto.getSteps())
                .instructions(dto.getInstructions())
                .ingredients(dto.getIngredients())
                .richTextContent(dto.getRichTextContent())
                .build();

        recipeRepository.save(recipe);

        return recipe;
    }
}
