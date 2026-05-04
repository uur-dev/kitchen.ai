package com.br3akPoint.ai_service.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecipeAIResponse {

    @JsonProperty("status")
    private Boolean status; // true = success, false = error

    @JsonProperty("reason")
    private String reason; // null on success, error key on failure

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("prepTimeMinutes")
    private int prepTimeMinutes;

    @JsonProperty("cookTimeMinutes")
    private int cookTimeMinutes;

    @JsonProperty("servings")
    private int servings;

    @JsonProperty("difficulty")
    private String difficulty;

    @JsonProperty("cuisine")
    private String cuisine;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("ingredients")
    private List<Ingredient> ingredients;

    @JsonProperty("steps")
    private List<Step> steps;

    @JsonProperty("nutritionInfo")
    private NutritionInfo nutritionInfo;

    // ── Nested: Ingredient ──────────────────────────────────────────────────

    @Getter
    @Setter
    public static class Ingredient {

        @JsonProperty("name")
        private String name;

        @JsonProperty("quantity")
        private String quantity;

        @JsonProperty("unit")
        private String unit;

        @JsonProperty("notes")
        private String notes;

        public Map<String, Object> getMap() {
            return Map.of(
                    "name", name,
                    "quantity", quantity,
                    "unit", unit,
                    "notes", notes
            );
        }
    }

    // ── Nested: Step ────────────────────────────────────────────────────────

    @Getter
    @Setter
    public static class Step {

        @JsonProperty("stepNumber")
        private int stepNumber;

        @JsonProperty("instruction")
        private String instruction;

        @JsonProperty("durationMinutes")
        private Integer durationMinutes;

        @JsonProperty("tip")
        private String tip;

        public Map<String, Object> getMap() {
            return Map.of(
                    "stepNumber", stepNumber,
                    "instruction", instruction,
                    "durationMinutes", durationMinutes,
                    "tip", tip
            );
        }
    }

    // ── Nested: NutritionInfo ───────────────────────────────────────────────

    @Getter
    @Setter
    public static class NutritionInfo {

        @JsonProperty("caloriesPerServing")
        private int caloriesPerServing;

        @JsonProperty("proteinGrams")
        private double proteinGrams;

        @JsonProperty("carbsGrams")
        private double carbsGrams;

        @JsonProperty("fatGrams")
        private double fatGrams;

        @JsonProperty("fiberGrams")
        private double fiberGrams;

        public Map<String, Object> getMap() {
            return Map.of(
                    "caloriesPerServing", caloriesPerServing,
                    "proteinGrams", proteinGrams,
                    "fatGrams", fatGrams,
                    "fiberGrams", fiberGrams
            );
        }
    }
}