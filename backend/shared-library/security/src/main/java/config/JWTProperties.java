package config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JWTProperties {
    private String secret;
    private long   accessTokenExpiryMs  = 5L * 60 * 1000;          // default 5 min
    private long   refreshTokenExpiryMs = 30L * 24 * 60 * 60 * 1000; // default 30 days
}
