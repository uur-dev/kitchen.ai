package response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApiError {

    @Builder.Default
    private List<String> errorList = new ArrayList<>();
    private String reason;

    public String firstError() {
        if (errorList == null || errorList.isEmpty()) return null;
        return errorList.get(0);
    }

    public static ApiError of(String... errors) {
        return ApiError.builder().errorList(List.of(errors)).build();
    }

    public static ApiError of(String error, String reason) {
        return ApiError.builder().errorList(List.of(error)).reason(reason).build();
    }
}
