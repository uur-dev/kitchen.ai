package com.br3akPoint.notification_service.repository;

import com.br3akPoint.notification_service.entity.UserPushToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPushTokenRepo extends MongoRepository<UserPushToken, String> {
    @Query("{ 'userId': ?0, 'fcmToken': ?1, 'deviceId': ?2, 'deviceType': ?3, 'isValid': ?4 }")
    public Optional<UserPushToken> findToken(Long userId, String fcmToken, String deviceId, String deviceType, Boolean status);

    @Query("{ 'userId': ?0, 'isValid': true }")
    public List<UserPushToken> findAllByUserId(Long userId);
}
