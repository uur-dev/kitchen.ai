package config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import util.JWTUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class JWTAutoConfig {

    @Bean
    @ConfigurationProperties(prefix = "jwt")   // ← reads from each service's own properties
    public JWTProperties jwtProperties() {
        return new JWTProperties();
    }

    @Bean
    public JWTUtil jwtUtil(JWTProperties props) {
        return new JWTUtil(
                props.getSecret(),
                props.getAccessTokenExpiryMs(),
                props.getRefreshTokenExpiryMs()
        );
    }
}
