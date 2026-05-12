package data.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RecipeResult {
    private String exception;
    private String error;
    private RecipeShortInfo info;

    public boolean hasInfo() {
        return info != null;
    }

    public boolean hasError() {
        return error != null;
    }

    public boolean hasException() {
        return exception != null;
    }

    public boolean hasIssue() {
        return hasError() || hasException();
    }

    public String getIssue() {
        if(hasError()) {
            return error;
        } else if(hasException()) {
            return exception;
        } else {
             return null;
        }
    }

    public static RecipeResult fromError(String error) {
        return RecipeResult.builder()
                .error(error)
                .build();
    }

    public static RecipeResult fromException(String exception) {
        return RecipeResult.builder()
                .exception(exception)
                .build();
    }

    public static RecipeResult forSuccess(SaveRecipeDTO dto) {
        return RecipeResult.builder()
                .info(RecipeShortInfo.builder()
                        .userId(dto.getUserId())
                        .requestId(dto.getRequestId())
                        .recipeTitle(dto.getTitle())
                        .recipeDescription(dto.getDescription())
                        .preparationTime(dto.getPrepTimeMins())
                        .cookingTime(dto.getCookTimeMins())
                        .serving(dto.getServings())
                        .difficulty(dto.getDifficulty())
                        .ingredients(dto.getIngredientsList())
                        .build())
                .build();
    }

    public static RecipeResult fromMap(Map<String, Object> map) {
        try {
            if(map.containsKey("error")) {
                return RecipeResult.fromError((String) map.get("error"));
            } else if(map.containsKey("exception")) {
                return RecipeResult.fromException((String) map.get("exception"));
            } else {
                return RecipeResult.builder()
                        .info(RecipeShortInfo.fromMap(map))
                        .build();
            }
        } catch (Exception exception) {
            return RecipeResult.builder().build();
        }
    }

}

