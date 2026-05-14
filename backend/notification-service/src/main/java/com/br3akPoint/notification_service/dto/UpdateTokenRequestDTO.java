package com.br3akPoint.notification_service.dto;

import com.br3akPoint.notification_service.constant.ValidationError;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateTokenRequestDTO {
    @NotBlank(message = ValidationError.FCM_Token_Required)
    @NotNull(message = ValidationError.FCM_Token_Required)
    private String fcmToken;
}
