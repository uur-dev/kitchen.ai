package com.br3akPoint.auth_service.entity;

import com.br3akPoint.auth_service.constant.DeviceTypeEnum;
import com.br3akPoint.entity.BaseEntity;
import com.br3akPoint.util.UnixTimestampSerializer;
import jakarta.persistence.*;
import lombok.*;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.Instant;

@Entity
@Table(name = "auth_session")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthSession extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    private DeviceTypeEnum deviceType;

    @Column(name = "refresh_token", nullable = false, unique = true)
    private String refreshToken;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "expiry", nullable = false)
    @JsonSerialize(using = UnixTimestampSerializer.class)
    private Instant expiry;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}
