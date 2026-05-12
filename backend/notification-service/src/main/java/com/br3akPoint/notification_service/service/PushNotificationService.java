package com.br3akPoint.notification_service.service;

import com.br3akPoint.notification_service.dto.BasePushNotification;
import com.br3akPoint.notification_service.entity.UserPushToken;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final ObjectMapper objectMapper;

    public void sendNotification(UserPushToken userPushToken, BasePushNotification pushNotification, Object payload) {
        try {
            String jsonStringPayload = "{}";
            try {
                jsonStringPayload = objectMapper.writeValueAsString(payload);
            }catch (Exception ignored) {}


            // 2. Build the message
            var messagBuilder = Message.builder()
                    .setToken(userPushToken.getFcmToken())
                    .putData("json_data", jsonStringPayload)
                    .putData("type", pushNotification.getNotificationType());

            setDeviceConfig(messagBuilder, pushNotification, userPushToken.getDeviceType());

            Message message = messagBuilder.build();

            // 3. Send via FirebaseMessaging
            firebaseMessaging.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setDeviceConfig(Message.Builder builder, BasePushNotification notification, String deviceType) {
        if(deviceType.equalsIgnoreCase("android")) { // Use equalsIgnoreCase for flexibility
            builder.setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setTitle(notification.getNotificationTitle())
                                    .setBody(notification.getNotificationBody())
                                    .build())
                    .build());
        } else if(deviceType.equalsIgnoreCase("ios")) { // Use equalsIgnoreCase for flexibility
            builder.setApnsConfig(ApnsConfig.builder()
                    .setAps(Aps.builder()
                            .setAlert(ApsAlert.builder()
                                    .setTitle(notification.getNotificationTitle())
                                    .setBody(notification.getNotificationBody())
                                    .build())
                            .setSound("default") // Common practice for iOS notifications
                            .build())
                    .build());
        } else if (deviceType.equalsIgnoreCase("web") ||
                   deviceType.equalsIgnoreCase("macOS") ||
                   deviceType.equalsIgnoreCase("desktop")) {
            // For web, macOS, and Windows, we can use WebpushConfig.
            // macOS and Windows desktop apps often use webpush-like mechanisms if they are PWAs or Electron apps.
            builder.setWebpushConfig(WebpushConfig.builder()
                    .setNotification(new WebpushNotification(
                            notification.getNotificationTitle(),
                            notification.getNotificationBody(),
                            null // icon, can be set if available
                    ))
                    .build());
        }
    }
}
