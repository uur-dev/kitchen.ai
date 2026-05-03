package response;

import lombok.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ApiResponse<T> {

    private T data;
    private int statusCode;
    private boolean status;
    private ApiError error;

    public boolean hasData() {
        return data != null;
    }

    public boolean hasError() {
        return error != null;
    }

    // ── Success ──────────────────────────────────────────────────────────────

    public static ApiResponse<?> statusOk() {
        return ApiResponse.builder()
                .statusCode(200)
                .status(true)
                .build();
    }

    public static <T> ApiResponse<T> response(int statusCode, T data) {
        return ApiResponse.<T>builder()
                .statusCode(statusCode)
                .status(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> responseData(T data) {
        return ApiResponse.<T>builder()
                .statusCode(200)
                .status(true)
                .data(data)
                .build();
    }

    // ── Error ─────────────────────────────────────────────────────────────────

    private static ApiResponse<?> buildError(int statusCode, ApiError error) {
        return ApiResponse.builder()
                .error(error)
                .status(false)
                .statusCode(statusCode)
                .build();
    }

    public static ApiResponse<?> error(int statusCode, ApiError error) {
        return buildError(statusCode, error);
    }

    public static ApiResponse<?> error(int statusCode, List<String> errors) {
        return buildError(statusCode, ApiError.builder().errorList(errors).build());
    }

    public static ApiResponse<?> error(int statusCode, String message) {
        return buildError(statusCode, ApiError.of(message));
    }

    // ── Exception ─────────────────────────────────────────────────────────────

    public static ApiResponse<?> exception(String exception, String trace) {
        return exception(500, exception, trace);
    }

    public static ApiResponse<?> exception(int statusCode, String exception, String trace) {
        return buildError(statusCode, ApiError.of(exception, trace));
    }
}