package com.br3akPoint.auth_service.repository;

import com.br3akPoint.auth_service.constant.DeviceTypeEnum;
import com.br3akPoint.auth_service.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {
    @Query("SELECT s FROM AuthSession s WHERE s.refreshToken = :refreshToken " +
            "AND s.deviceId = :deviceId " +
            "AND s.deviceType = :deviceType " +
            "AND s.expiry > :now " +
            "AND s.enabled IS TRUE")
    Optional<AuthSession> findSession(@Param("refreshToken") String refreshToken,
                                      @Param("deviceId") String deviceId,
                                      @Param("deviceType") DeviceTypeEnum deviceType,
                                      @Param("now") Instant now);
}
