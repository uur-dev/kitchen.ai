package com.br3akPoint.recipe_service.controller;

import com.br3akPoint.recipe_service.entity.Recipe;
import com.br3akPoint.recipe_service.service.RecipeService;
import data.dto.SaveRecipeDTO;
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

    @PostMapping("recipe/add/new")
    public ResponseEntity<ApiResponse<Recipe>> addNewRecipe(@RequestBody SaveRecipeDTO dto) throws Exception {
        Recipe recipe = recipeService.createNewRecipe(dto);
        return ResponseEntity.ok(ApiResponse.responseData(recipe));
    }

}
