package com.br3akPoint.notification_service.listener;

import com.br3akPoint.notification_service.entity.UserPushToken;
import com.br3akPoint.notification_service.service.UserPushTokenService;
import constant.MessageBrokerKeys;
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

    @RabbitListener(queues = MessageBrokerKeys.NOTIFICATION_QUEUE_NAME)
    public void recipeRequestProcessCompleted(EventRecipeProcessCompleted event) {
        try {
            log.info("Event Received for Notification recipeId = {}, status={}, summary={}", event.getRequestId(), event.getStatus(), event.getSummary());

            //get all fcm token for user
            var userActiveTokens = userPushTokenService.getAllUserToken();
            if(!userActiveTokens.isEmpty()) {
                for(var eachToken : userActiveTokens) {
                    //send notification
                    sendNotification(eachToken, event);
                }
            }
        } catch (Exception e) {
            log.error("Error in notification for recipe requestId={}", event.getRequestId());
        }
    }

    private void sendNotification(UserPushToken token, EventRecipeProcessCompleted event) {
        try {
            log.info("Notification Sent to userId={} for deviceType={} and deviceId={}", token.getUserId(), token.getDeviceType(), token.getDeviceId());
        } catch (Exception ex) {
            log.error("Notification Error: userId={}, deviceType={}, deviceId={}", token.getUserId(), token.getDeviceType(), token.getDeviceType());
        }
    }
}
