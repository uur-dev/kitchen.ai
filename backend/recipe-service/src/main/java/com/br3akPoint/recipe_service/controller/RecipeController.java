package com.br3akPoint.recipe_service.controller;

import com.br3akPoint.recipe_service.data.dto.request.CreateRecipeRequestDTO;
import com.br3akPoint.recipe_service.data.dto.request.RecipeRequestDTO;
import com.br3akPoint.recipe_service.entity.Recipe;
import com.br3akPoint.recipe_service.entity.RecipeRequest;
import com.br3akPoint.recipe_service.service.RecipeService;
import com.br3akPoint.response.ApiResponse;
import com.br3akPoint.util.UserContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipe")
public class RecipeController {

    private final RecipeService recipeService;

    @Autowired public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<RecipeRequest>> recipeRequest(@Valid @RequestBody RecipeRequestDTO dto) {
        var recipeRequest = recipeService.addRecipeRequest(dto.getContent(), dto.getRequestType());
        return ResponseEntity.ok(ApiResponse.responseData(recipeRequest));
    }

    @GetMapping("request")
    public ResponseEntity<ApiResponse<List<RecipeRequest>>> getAllRequest(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "count", defaultValue = "10") int count) {
        var list = recipeService.getAllByUserId(UserContext.getUserId(), page, count);
        return ResponseEntity.ok(ApiResponse.responseData(list));
    }

    @PostMapping("/add/new")
    public ResponseEntity<ApiResponse<Recipe>> addNewRecipe(@Valid @RequestBody CreateRecipeRequestDTO dto) throws Exception {
        var recipe = recipeService.createNewRecipe(dto);
        return ResponseEntity.ok(ApiResponse.responseData(recipe));
    }
}
