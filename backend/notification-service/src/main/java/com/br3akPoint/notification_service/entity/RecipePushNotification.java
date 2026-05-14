package com.br3akPoint.notification_service.entity;

import com.br3akPoint.notification_service.dto.BasePushNotification;
import data.dto.RecipeResult;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_notifications")
@Setter
@Getter
@Builder
public class RecipePushNotification extends BasePushNotification {

    private Long userId;
    private String title;
    private String body;
    private RecipeResult recipeResult;
    private Instant dateTime;

    @Override
    public String getNotificationTitle() {
        return title;
    }

    @Override
    public String getNotificationBody() {
        return body;
    }

    @Override
    public String getNotificationType() {
        return "recipe_generation";
    }
}
