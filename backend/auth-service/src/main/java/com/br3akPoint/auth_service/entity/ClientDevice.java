package com.br3akPoint.auth_service.entity;

import com.br3akPoint.auth_service.constant.DeviceTypeEnum;
import com.br3akPoint.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "client_devices")
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ClientDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    private DeviceTypeEnum deviceType;

    @Column(name = "app_id", nullable = false, unique = true)
    private String appId;

    @Column(name = "app_secret", nullable = false)
    private String appSecret;
}
