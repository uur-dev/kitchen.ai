package com.br3akPoint.ai_service.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {
    @Bean
    Client generateImageClient() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }
}
