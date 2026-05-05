package data.dto;

import lombok.*;

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
}
