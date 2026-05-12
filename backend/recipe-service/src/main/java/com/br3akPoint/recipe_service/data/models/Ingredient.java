package com.br3akPoint.recipe_service.data.models;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    private String name;
    private String quantity;
    private String unit;
    private String notes;
}
