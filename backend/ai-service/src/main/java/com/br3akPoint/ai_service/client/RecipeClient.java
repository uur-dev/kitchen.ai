package com.br3akPoint.ai_service.client;


import data.dto.SaveRecipeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.ApiResponse;

@FeignClient(name = "recipe-service")
public interface RecipeClient {
    @PostMapping("internal/recipe/add/new")
    public ApiResponse<?> saveRecipe(@RequestBody SaveRecipeDTO dto);
}
