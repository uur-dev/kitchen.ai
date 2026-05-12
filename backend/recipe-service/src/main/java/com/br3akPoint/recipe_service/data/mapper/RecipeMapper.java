package com.br3akPoint.recipe_service.data.mapper;

import com.br3akPoint.recipe_service.constant.RecipeRequestType;
import com.br3akPoint.recipe_service.data.models.Ingredient;
import com.br3akPoint.recipe_service.data.models.NutritionInfo;
import com.br3akPoint.recipe_service.data.models.RecipeStep;
import com.br3akPoint.recipe_service.entity.Recipe;
import com.br3akPoint.recipe_service.entity.RecipeRequest;
import data.dto.SaveRecipeDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecipeMapper {

    public static Recipe toEntity(SaveRecipeDTO dto, RecipeRequest recipeRequest) {
        return Recipe.builder()
                .userId(dto.getUserId())
                .requestId(recipeRequest.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .prepTimeMins(dto.getPrepTimeMins())
                .cookTimeMins(dto.getCookTimeMins())
                .cuisine(dto.getCuisine())
                .difficulty(dto.getDifficulty())
                .servings(dto.getServings())
                .requestType(RecipeRequestType.valueOf(dto.getRequestType()))
                .ingredientsList(dto.getIngredientsList())
                .tags(dto.getTags())
                .ingredients(mapIngredients(dto.getIngredients()))
                .steps(mapSteps(dto.getSteps()))
                .nutritionInfo(mapNutritionInfo(dto.getNutritionInfo()))
                .build();
    }

    // Map<String, Object> → Ingredient
    private static List<Ingredient> mapIngredients(List<Map<String, Object>> raw) {
        if (raw == null) return null;
        return raw.stream().map(m -> Ingredient.builder()
                .name((String) m.get("name"))
                .quantity((String) m.get("quantity"))
                .unit((String) m.get("unit"))
                .notes((String) m.get("notes"))
                .build()
        ).collect(Collectors.toList());
    }

    // Map<String, Object> → RecipeStep
    private static List<RecipeStep> mapSteps(List<Map<String, Object>> raw) {
        if (raw == null) return null;
        return raw.stream().map(m -> RecipeStep.builder()
                .stepNumber(toInt(m.get("stepNumber")))
                .instruction((String) m.get("instruction"))
                .durationMinutes(toInt(m.get("durationMinutes")))
                .tip((String) m.get("tip"))
                .build()
        ).collect(Collectors.toList());
    }

    // Map<String, Object> → NutritionInfo
    private static NutritionInfo mapNutritionInfo(Map<String, Object> m) {
        if (m == null) return null;
        return NutritionInfo.builder()
                .caloriesPerServing(toInt(m.get("caloriesPerServing")))
                .proteinGrams(toBigDecimal(m.get("proteinGrams")))
                .carbsGrams(toBigDecimal(m.get("carbsGrams")))
                .fatGrams(toBigDecimal(m.get("fatGrams")))
                .fiberGrams(toBigDecimal(m.get("fiberGrams")))
                .build();
    }

    // Helper: Object → Integer (JSON mein numbers Integer ya Double aa sakte hain)
    private static Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.parseInt(val.toString());
    }

    // Helper: Object → BigDecimal
    private static BigDecimal toBigDecimal(Object val) {
        if (val == null) return null;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        return new BigDecimal(val.toString());
    }
}