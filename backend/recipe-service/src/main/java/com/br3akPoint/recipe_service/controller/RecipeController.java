package com.br3akPoint.recipe_service.controller;

import com.br3akPoint.recipe_service.data.dto.request.RecipeRequestDTO;
import com.br3akPoint.recipe_service.entity.RecipeRequest;
import com.br3akPoint.recipe_service.service.RecipeCuisineService;
import com.br3akPoint.recipe_service.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import response.ApiResponse;
import util.UserContext;

@RestController
@RequestMapping("/recipe")
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeCuisineService cuisineService;

    @Autowired public RecipeController(RecipeService recipeService, RecipeCuisineService cuisineService) {
        this.recipeService = recipeService;
        this.cuisineService = cuisineService;
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<RecipeRequest>> recipeRequest(@Valid @RequestBody RecipeRequestDTO dto) {
        var recipeRequest = recipeService.addRecipeRequest(dto.getContent(), dto.getRequestType(), dto.getCuisine());
        return ResponseEntity.ok(ApiResponse.responseData(recipeRequest));
    }

    @GetMapping("/request")
    public ResponseEntity<ApiResponse<?>> getAllRequest(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "count", defaultValue = "10") int count) {
        var list = recipeService.getAllRequestsByUserId(UserContext.getUserId(), page, count);
        return ResponseEntity.ok(ApiResponse.responseData(list));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllRecipe(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "count", defaultValue = "10") int count) {
        var list = recipeService.getAllByUserId(UserContext.getUserId(), page, count);
        return ResponseEntity.ok(ApiResponse.responseData(list));
    }

    @GetMapping("cuisine")
    public ResponseEntity<ApiResponse<?>> getAllCuisine() {
        var list = cuisineService.getAll();
        return ResponseEntity.ok(ApiResponse.responseData(list));
    }

    @DeleteMapping("/remove/{recipeId}")
    public ResponseEntity<ApiResponse<?>> removeRecipe(@PathVariable Long recipeId) throws Exception {
        recipeService.removeRecipe(recipeId);
        return ResponseEntity.ok(ApiResponse.statusOk());
    }

    @DeleteMapping("/request/remove/{requestId}")
    public ResponseEntity<ApiResponse<?>> removeRecipeRequest(@PathVariable Long requestId) throws Exception {
        recipeService.removeRecipeRequest(requestId);
        return ResponseEntity.ok(ApiResponse.statusOk());
    }
}
