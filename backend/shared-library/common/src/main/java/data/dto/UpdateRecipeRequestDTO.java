package data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateRecipeRequestDTO {
    private Long userId;
    private Long requestId;
    private String status;
    private String reason;
}
