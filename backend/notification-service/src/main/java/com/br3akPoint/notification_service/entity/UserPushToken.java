package com.br3akPoint.notification_service.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_push_notification")
@Data
@Builder
public class UserPushToken {
    private Long userId;
    private String fcmToken;
    private String deviceType;
    private String deviceId;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isValid;
}
