package data.dto;

import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaveRecipeDTO {
    private Long userId;
    private Long requestId;
    private String title;
    private String image;
    private String description;
    private Integer prepTimeMins;
    private Integer cookTimeMins;
    private String cuisine;
    private String difficulty;
    private Integer servings;
    private String requestType;
    private List<String> ingredientsList;
    private List<String> tags;
    private List<Map<String, Object>> ingredients;
    private List<Map<String, Object>> steps;
    private Map<String, Object> nutritionInfo;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("requestId", requestId);
        map.put("title", title);
        map.put("description", description);
        map.put("prepTimeMins", prepTimeMins);
        map.put("cookTimeMins", cookTimeMins);
        map.put("cuisine", cuisine);
        map.put("difficulty", difficulty);
        map.put("servings", servings);
        map.put("requestType", requestType);
        map.put("ingredientsList", ingredientsList);
        map.put("tags", tags);
        map.put("ingredients", ingredients);
        map.put("steps", steps);
        map.put("nutritionInfo", nutritionInfo);
        return map;
    }
}
