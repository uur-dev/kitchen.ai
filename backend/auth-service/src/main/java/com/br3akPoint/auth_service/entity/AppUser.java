package com.br3akPoint.auth_service.entity;

import com.br3akPoint.auth_service.constant.SocialProviderEnum;
import com.br3akPoint.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_auth")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppUser extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // password hide in response
    private String password;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider")
    private SocialProviderEnum socialProvider;

    @Column(name = "social_id")
    private String socialId;
}
