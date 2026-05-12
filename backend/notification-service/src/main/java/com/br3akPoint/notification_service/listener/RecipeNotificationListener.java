package com.br3akPoint.notification_service.listener;

import com.br3akPoint.notification_service.entity.RecipePushNotification;
import com.br3akPoint.notification_service.entity.UserPushToken;
import com.br3akPoint.notification_service.repository.NotificationRepository;
import com.br3akPoint.notification_service.service.PushNotificationService;
import com.br3akPoint.notification_service.service.UserPushTokenService;
import constant.MessageBrokerKeys;
import constant.RecipeStatus;
import data.dto.RecipeResult;
import event.EventRecipeProcessCompleted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecipeNotificationListener {

    private final UserPushTokenService userPushTokenService;
    private final NotificationRepository notificationRepository;
    private final PushNotificationService pushNotificationService;

    @RabbitListener(queues = MessageBrokerKeys.NOTIFICATION_QUEUE_NAME)
    public void recipeRequestProcessCompleted(EventRecipeProcessCompleted event) {
        try {
            log.info("Event Received for Notification recipeId = {}, status={}, summary={}", event.getRequestId(), event.getStatus(), event.getSummary());

            RecipePushNotification notification = getNotification(event);
            //save notification in mongo
            saveNotification(notification);
            //get all fcm token for user
            var userActiveTokens = userPushTokenService.getAllUserToken(event.getUserId());
            if(!userActiveTokens.isEmpty()) {
                for(var eachToken : userActiveTokens) {
                    //send notification
                    sendNotification(eachToken, notification);
                }
            }
        } catch (Exception e) {
            log.error("Error in notification for recipe requestId={}", event.getRequestId());
        }
    }

    private void sendNotification(UserPushToken token, RecipePushNotification notification) {
        try {
            log.info("Notification Sent to userId={} for deviceType={} and deviceId={}", token.getUserId(), token.getDeviceType(), token.getDeviceId());
            pushNotificationService.sendNotification(token, notification, notification.getRecipeResult());

        } catch (Exception ex) {
            log.error("Notification Error: userId={}, deviceType={}, deviceId={}", token.getUserId(), token.getDeviceType(), token.getDeviceType());
        }
    }

    private void saveNotification(RecipePushNotification notification) {
        try {
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Notification Saving Exception: {}", e.getMessage());
        }
    }

    private RecipePushNotification getNotification(EventRecipeProcessCompleted event) {
        boolean isFailed = event.getStatus().equals(RecipeStatus.failed.name());
        return RecipePushNotification.builder()
                .userId(event.getUserId())
                .title(isFailed ? RecipeStatus.failed.getNotificationTitle() : RecipeStatus.completed.getNotificationTitle())
                .recipeResult(RecipeResult.fromMap(event.getResult()))
                .body(event.getSummary())
                .build();
    }
}
