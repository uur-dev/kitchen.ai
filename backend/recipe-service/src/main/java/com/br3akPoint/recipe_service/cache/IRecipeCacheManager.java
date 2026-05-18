package com.br3akPoint.recipe_service.cache;

import com.br3akPoint.recipe_service.entity.Recipe;
import com.br3akPoint.recipe_service.entity.RecipeRequest;
import response.PageData;

import java.util.Optional;

public interface IRecipeCacheManager {
    public Optional<PageData<Recipe>> recipeGetPaged(Long userId, int page, int count);
    public Optional<PageData<RecipeRequest>> recipeRequestGetPaged(Long userId, int page, int count);
    public void recipePutPaged(Long userId, int page, int count, PageData<Recipe> data);
    public void recipeRequestPutPaged(Long userId, int page, int count, PageData<RecipeRequest> data);
    public void recipeInvalidate(Long userId);
    public void recipeRequestInvalidate(Long userId);
}