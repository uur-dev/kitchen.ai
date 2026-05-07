package com.br3akPoint.recipe_service.controller;

import com.br3akPoint.recipe_service.entity.Recipe;
import com.br3akPoint.recipe_service.entity.RecipeRequest;
import com.br3akPoint.recipe_service.service.RecipeService;
import data.dto.SaveRecipeDTO;
import data.dto.UpdateRecipeRequestDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import response.ApiResponse;

@RestController
@RequestMapping("internal")
@AllArgsConstructor
public class InternalController {

    private final RecipeService recipeService;

    @PostMapping("/recipe/add/new")
    public ResponseEntity<ApiResponse<Recipe>> addNewRecipe(@RequestBody SaveRecipeDTO dto) throws Exception {
        Recipe recipe = recipeService.createNewRecipe(dto);
        return ResponseEntity.ok(ApiResponse.responseData(recipe));
    }

    @PostMapping("/recipe/request/update")
    public ResponseEntity<ApiResponse<RecipeRequest>> updateRecipeRequest(@RequestBody UpdateRecipeRequestDTO dto) throws Exception{
        RecipeRequest recipeRequest = recipeService.updateRecipeRequest(dto);
        return ResponseEntity.ok(ApiResponse.responseData(recipeRequest));
    }

}
