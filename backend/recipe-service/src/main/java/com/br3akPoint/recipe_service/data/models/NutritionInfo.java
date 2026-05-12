package com.br3akPoint.recipe_service.data.models;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionInfo {
    private Integer caloriesPerServing;
    private BigDecimal proteinGrams;
    private BigDecimal carbsGrams;
    private BigDecimal fatGrams;
    private BigDecimal fiberGrams;
}
