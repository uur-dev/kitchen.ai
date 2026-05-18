package cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheEntity {
    RECIPE("recipe"),
    RECIPE_CUISINE("recipe_cuisine"),
    RECIPE_REQUEST("recipe_request");

    private final String value;
}
