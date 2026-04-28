package com.br3akPoint.config;

import com.br3akPoint.security.JWTProperties;
import com.br3akPoint.util.JWTUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    @ConditionalOnMissingBean                  // ← service can override if needed
    public JWTUtil jwtUtil(JWTProperties props) {
        return new JWTUtil(
                props.getSecret(),
                props.getAccessTokenExpiryMs(),
                props.getRefreshTokenExpiryMs()
        );
    }
}
