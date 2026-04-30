package com.br3akPoint.auth_service.data;

import lombok.*;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceContext {
    private String deviceId;
    private String deviceType;
}
