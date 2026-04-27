package com.br3akPoint.auth_service.repository;

import com.br3akPoint.auth_service.entity.ClientDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientDeviceRepository extends JpaRepository<ClientDevice, Long> {
    public Optional<ClientDevice> findByAppId(String appId);
}
