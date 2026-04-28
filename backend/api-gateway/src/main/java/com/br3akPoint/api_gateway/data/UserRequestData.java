package com.br3akPoint.api_gateway.data;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequestData {
    private String email;
    private Long userId;
    private String deviceType;
}
