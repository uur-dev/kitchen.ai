package com.br3akPoint.notification_service.service;

import com.br3akPoint.notification_service.constant.ServerError;
import com.br3akPoint.notification_service.entity.UserPushToken;
import com.br3akPoint.notification_service.repository.UserPushTokenRepo;
import error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import util.UserContext;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserPushTokenService {
    private final UserPushTokenRepo userPushTokenRepo;

    public Optional<UserPushToken> findByToken(String token, Boolean status) {
        return userPushTokenRepo.findToken(
                UserContext.getUserId(),
                token,
                UserContext.getDeviceId(),
                UserContext.getDeviceType(),
                status != null ? status : true
        );
    }

    public List<UserPushToken> getAllUserToken() {
        return userPushTokenRepo.findAllByUserId(UserContext.getUserId());
    }

    public void updateUserFcmToken(String token) {
        var userPushToken = findByToken(token, null);
        if(userPushToken.isEmpty()) {
            //first time update
            var newToken = UserPushToken.builder()
                    .userId(UserContext.getUserId())
                    .fcmToken(token)
                    .deviceId(UserContext.getDeviceId())
                    .deviceType(UserContext.getDeviceType())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .isValid(true)
                    .build();
            userPushTokenRepo.save(newToken);
        } else {
            //exist but need to update again
            userPushToken.get().setFcmToken(token);
            userPushToken.get().setUpdatedAt(Instant.now());
            userPushTokenRepo.save(userPushToken.get());
        }

    }

    public void toggleFcmTokenStatus(String token) throws Exception {
        var userPushToken = findByToken(token, null);
        if(userPushToken.isEmpty()) {
            throw BusinessException.notFound(ServerError.User_Push_Token_Not_Found);
        }

        userPushToken.get().setIsValid(!userPushToken.get().getIsValid());
        userPushTokenRepo.save(userPushToken.get());
    }

    public void deleteFcmToken(String token) throws Exception {
        var userPushToken = findByToken(token, null);
        if(userPushToken.isEmpty()) {
            throw BusinessException.notFound(ServerError.User_Push_Token_Not_Found);
        }

        userPushTokenRepo.delete(userPushToken.get());
    }
}
