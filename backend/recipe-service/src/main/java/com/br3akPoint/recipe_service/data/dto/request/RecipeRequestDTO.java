package com.br3akPoint.recipe_service.data.dto.request;

import com.br3akPoint.recipe_service.constant.RecipeRequestType;
import com.br3akPoint.recipe_service.constant.ValidationConstant;
import com.br3akPoint.recipe_service.entity.RecipeCuisine;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import util.validator.ValidEnum;
import validation.ExistsInDatabase;

@Data
@Getter
public class RecipeRequestDTO {
    @NotBlank(message = ValidationConstant.Content_Required)
    @NotNull(message = ValidationConstant.Content_Required)
    private String content;

    @NotNull(message = ValidationConstant.Request_Type_Required)
    @ValidEnum(enumClass = RecipeRequestType.class, message = ValidationConstant.Request_Type_Invalid)
    private String requestType;

    @ExistsInDatabase(
            entity = RecipeCuisine.class,
            field = "value",
            message = ValidationConstant.Cuisine_Invalid
    )
    private String cuisine;
}
