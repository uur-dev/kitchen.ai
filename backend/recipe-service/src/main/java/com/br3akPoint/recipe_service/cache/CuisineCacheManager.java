package com.br3akPoint.recipe_service.cache;

import cache.CacheEntity;
import cache.GenericCacheManager;
import com.br3akPoint.recipe_service.entity.RecipeCuisine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CuisineCacheManager implements ICuisineCacheManager {

    private final GenericCacheManager cacheManager;

    @Override
    public Optional<List<RecipeCuisine>> getAll() {
        return cacheManager.getList(CacheEntity.RECIPE_CUISINE.getValue());
    }

    @Override
    public void putAll(List<RecipeCuisine> list) {
        cacheManager.putList(CacheEntity.RECIPE_CUISINE.getValue(), list);
    }
}
