package com.br3akPoint.notification_service.repository;

import com.br3akPoint.notification_service.entity.RecipePushNotification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends MongoRepository<RecipePushNotification, String> {
}
