package event;


import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventRecipeRequestCreated {
    private Long userId;
    private Long requestId;
    private String content;
    private String type;
}