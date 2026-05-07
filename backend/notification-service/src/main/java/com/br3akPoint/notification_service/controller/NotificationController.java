package com.br3akPoint.notification_service.controller;

import com.br3akPoint.notification_service.dto.UpdateTokenRequestDTO;
import com.br3akPoint.notification_service.service.UserPushTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import response.ApiResponse;

@RequestMapping("/notification/fcm")
@RequiredArgsConstructor
@RestController
public class NotificationController {

    private final UserPushTokenService userPushTokenService;

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateFCMToken(@Valid @RequestBody UpdateTokenRequestDTO dto) {
        userPushTokenService.updateUserFcmToken(dto.getFcmToken());
        return ResponseEntity.ok(ApiResponse.statusOk());
    }

    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<?>> toggleTokenStatus(@Valid @RequestBody UpdateTokenRequestDTO dto) throws Exception {
        userPushTokenService.toggleFcmTokenStatus(dto.getFcmToken());
        return ResponseEntity.ok(ApiResponse.statusOk());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<?>> delete(@Valid @RequestBody UpdateTokenRequestDTO dto) throws Exception {
        userPushTokenService.deleteFcmToken(dto.getFcmToken());
        return ResponseEntity.ok(ApiResponse.statusOk());
    }

}
