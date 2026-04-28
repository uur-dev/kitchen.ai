package com.br3akPoint.api_gateway.config;

import com.br3akPoint.api_gateway.security.JWTAuthValidationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JWTAuthValidationFilter jwtAuthValidationFilter;

    @Autowired
    public SecurityConfig(JWTAuthValidationFilter jwtAuthValidationFilter) {
        this.jwtAuthValidationFilter = jwtAuthValidationFilter;
    }

    @Bean
    public FilterRegistrationBean<JWTAuthValidationFilter> disableClientDeviceFilterAutoRegistration() {
        FilterRegistrationBean<JWTAuthValidationFilter> registration = new FilterRegistrationBean<>(jwtAuthValidationFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authRouteFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/v1/api/auth/**")
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.anyRequest().permitAll();
                }).build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultRouteFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/v1/api/**")
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthValidationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
