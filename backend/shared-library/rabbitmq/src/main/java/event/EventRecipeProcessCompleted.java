package event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRecipeProcessCompleted {
    private Long userId;
    private Long requestId;
    private String status;
    private String summary;
    private Map<String, Object> result;
}

