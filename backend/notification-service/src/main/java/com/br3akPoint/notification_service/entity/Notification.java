package com.br3akPoint.notification_service.entity;

import data.dto.RecipeResult;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_notifications")
@Data
@Builder
public class Notification {
    private Long userId;
    private String title;
    private String body;
    private RecipeResult recipeResult;
    private Instant dateTime;
}
