package com.br3akPoint.recipe_service.service;

import com.br3akPoint.recipe_service.cache.CuisineCacheManager;
import com.br3akPoint.recipe_service.entity.RecipeCuisine;
import com.br3akPoint.recipe_service.repository.RecipeCuisineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeCuisineService {
    private final RecipeCuisineRepository repository;
    private final CuisineCacheManager cacheManager;

    public List<RecipeCuisine> getAll() {
        return cacheManager.getAll().orElseGet(()-> {
            List<RecipeCuisine> list = repository.findAll();
            cacheManager.putAll(list);
            return list;
        });
    }
}
