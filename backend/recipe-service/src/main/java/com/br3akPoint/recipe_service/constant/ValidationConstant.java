package com.br3akPoint.recipe_service.constant;

public class ValidationConstant {
    public final static String Request_Type_Required = "validation_error_recipe_request_required";
    public final static String Request_Type_Invalid = "validation_error_recipe_request_invalid";
    public final static String Content_Required = "validation_error_recipe_content_required";

    ///Recipe Create New Validation
    public final static String Recipe_UserId_Required = "validation_error_recipe_user_id_required";
    public final static String Recipe_RequestId_Required = "validation_error_recipe_request_id_required";
    public final static String Recipe_Title_Required = "validation_error_recipe_title_required";
    public final static String Recipe_Description_Required = "validation_error_recipe_description_required";
    public final static String Recipe_Duration_Required = "validation_error_recipe_duration_required";
    public final static String Recipe_Duration_Min = "validation_error_recipe_duration_min_limit";
    public final static String Recipe_Steps_Empty = "validation_error_recipe_step_empty";
    public final static String Recipe_Ingredients_Empty = "validation_error_recipe_ingredients_empty";
    public final static String Recipe_RichText_Empty = "validation_error_recipe_rich_text_empty";
}
