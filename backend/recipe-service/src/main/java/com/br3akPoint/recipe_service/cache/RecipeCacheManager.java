package com.br3akPoint.recipe_service.cache;

import cache.CacheEntity;
import cache.GenericCacheManager;
import com.br3akPoint.recipe_service.entity.Recipe;
import com.br3akPoint.recipe_service.entity.RecipeRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import response.PageData;
import java.util.Optional;

@Component
@AllArgsConstructor
public class RecipeCacheManager implements IRecipeCacheManager {

    private final GenericCacheManager cacheManager;

    @Override
    public Optional<PageData<Recipe>> recipeGetPaged(Long userId, int page, int count) {
        return cacheManager.getPaged(CacheEntity.RECIPE.getValue(), userId, page, count);
    }

    @Override
    public Optional<PageData<RecipeRequest>> recipeRequestGetPaged(Long userId, int page, int count) {
        return cacheManager.getPaged(CacheEntity.RECIPE_REQUEST.getValue(), userId, page, count);
    }

    @Override
    public void recipePutPaged(Long userId, int page, int count, PageData<Recipe> data) {
        cacheManager.putPaged(CacheEntity.RECIPE.getValue(), userId, page, count, data);
    }

    @Override
    public void recipeRequestPutPaged(Long userId, int page, int count, PageData<RecipeRequest> data) {
        cacheManager.putPaged(CacheEntity.RECIPE_REQUEST.getValue(), userId, page, count, data);
    }

    @Override
    public void recipeInvalidate(Long userId) {
        cacheManager.invalidateUser(CacheEntity.RECIPE.getValue(), userId);
    }

    @Override
    public void recipeRequestInvalidate(Long userId) {
        cacheManager.invalidateUser(CacheEntity.RECIPE_REQUEST.getValue(), userId);
    }
}