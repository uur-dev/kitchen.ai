package com.br3akPoint.recipe_service.data.dto.request;

import com.br3akPoint.recipe_service.constant.RecipeRequestType;
import com.br3akPoint.recipe_service.constant.ValidationConstant;
import com.br3akPoint.util.validator.ValidEnum;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateRecipeRequestDTO {
    @NotNull(message = ValidationConstant.Recipe_UserId_Required)
    private Long userId;

    @NotNull(message = ValidationConstant.Recipe_RequestId_Required)
    private Long requestId;

    @NotBlank(message = ValidationConstant.Recipe_Title_Required)
    @NotNull(message = ValidationConstant.Recipe_Title_Required)
    private String title;

    @NotBlank(message = ValidationConstant.Recipe_Description_Required)
    @NotNull(message = ValidationConstant.Recipe_Description_Required)
    private String description;

    @Min(value = 1, message = ValidationConstant.Recipe_Duration_Min)
    @NotNull(message = ValidationConstant.Recipe_Duration_Required)
    private Integer duration;

    @NotNull(message = ValidationConstant.Request_Type_Required)
    @ValidEnum(enumClass = RecipeRequestType.class, message = ValidationConstant.Request_Type_Invalid)
    private String requestType;

    @NotEmpty(message = ValidationConstant.Recipe_Steps_Empty)
    private List<String> steps;

    private List<String> instructions;

    @NotEmpty(message = ValidationConstant.Recipe_Ingredients_Empty)
    private List<String> ingredients;

    @NotBlank(message = ValidationConstant.Recipe_RichText_Empty)
    private String richTextContent;
}
