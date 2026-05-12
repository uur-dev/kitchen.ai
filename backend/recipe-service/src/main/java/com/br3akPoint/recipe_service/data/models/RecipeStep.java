package com.br3akPoint.recipe_service.data.models;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStep {
    private Integer stepNumber;
    private String instruction;
    private Integer durationMinutes;
    private String tip;
}
