package com.br3akPoint.recipe_service.entity;

import com.br3akPoint.recipe_service.constant.RecipeRequestType;
import com.br3akPoint.recipe_service.data.models.Ingredient;
import com.br3akPoint.recipe_service.data.models.NutritionInfo;
import com.br3akPoint.recipe_service.data.models.RecipeStep;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "recipe")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Recipe extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String image;

    @Column(name = "prep_time_mins")
    private Integer prepTimeMins;

    @Column(name = "cook_time_mins")
    private Integer cookTimeMins;

    @Column(length = 100)
    private String cuisine;

    @Column(length = 50)
    private String difficulty;

    private Integer servings;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", length = 20)
    private RecipeRequestType requestType;

    // TEXT[] — ingredient names only, for search
    @Column(name = "ingredients_list", columnDefinition = "text[]")
    private List<String> ingredientsList;

    // TEXT[] — tags
    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags;

    // JSONB — full ingredient objects
    @Column(name = "ingredients", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Ingredient> ingredients;

    // JSONB — full step objects
    @Column(name = "steps", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<RecipeStep> steps;

    // JSONB — nutrition
    @Column(name = "nutrition_info", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private NutritionInfo nutritionInfo;
}