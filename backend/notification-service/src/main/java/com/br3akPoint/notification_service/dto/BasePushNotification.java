package com.br3akPoint.notification_service.dto;

public abstract class BasePushNotification {
    public abstract String getNotificationTitle();
    public abstract String getNotificationBody();
    public abstract String getNotificationType();
}
