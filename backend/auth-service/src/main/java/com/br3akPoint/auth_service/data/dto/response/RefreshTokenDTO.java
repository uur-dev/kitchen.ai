package com.br3akPoint.auth_service.data.dto.response;

import lombok.*;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenDTO {
    private String refreshToken;
    private String accessToken;
}
