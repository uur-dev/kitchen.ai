package data.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class RecipeShortInfo {
    private Long userId;
    private Long requestId;
    private String recipeTitle;
    private String recipeDescription;
    private Integer cookingTime;
    private Integer preparationTime;
    private String difficulty;
    private Integer serving;
    private List<String> ingredients;

    @SuppressWarnings("unchecked")
    public static RecipeShortInfo fromMap(Map<String, Object> map) {
        if (map == null) return null;

        return RecipeShortInfo.builder()
                // Long values require careful casting from Map<String, Object>
                .userId(map.get("userId") != null ? ((Number) map.get("userId")).longValue() : null)
                .requestId(map.get("requestId") != null ? ((Number) map.get("requestId")).longValue() : null)

                // Mapping SaveRecipeDTO keys to RecipeShortInfo fields
                .recipeTitle((String) map.get("title"))
                .recipeDescription((String) map.get("description"))
                .cookingTime((Integer) map.get("cookTimeMins"))
                .preparationTime((Integer) map.get("prepTimeMins"))
                .difficulty((String) map.get("difficulty"))
                .serving((Integer) map.get("servings"))

                // Casting the List
                .ingredients((List<String>) map.get("ingredientsList"))
                .build();
    }
}
