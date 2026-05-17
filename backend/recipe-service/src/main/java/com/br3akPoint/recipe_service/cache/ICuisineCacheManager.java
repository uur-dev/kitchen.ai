package com.br3akPoint.recipe_service.cache;

import com.br3akPoint.recipe_service.entity.RecipeCuisine;

import java.util.List;
import java.util.Optional;

public interface ICuisineCacheManager {
    Optional<List<RecipeCuisine>> getAll();
    public void putAll(List<RecipeCuisine> list);
}
