package com.br3akPoint.auth_service.data.dto.response;

import lombok.*;
import tools.jackson.databind.annotation.JsonSerialize;
import util.UnixTimestampSerializer;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class LoginAuthDTO {
    private Long  userId;
    private String  email;
    private String  accessToken;
    private String  refreshToken;
    @JsonSerialize(using = UnixTimestampSerializer.class)
    private Instant refreshTokenExpiry;
    private String  fcmToken;
    private String  socialProvide;
    private String  providerId;
}