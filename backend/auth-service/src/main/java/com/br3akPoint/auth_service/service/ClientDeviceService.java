package com.br3akPoint.auth_service.service;

import com.br3akPoint.auth_service.constant.ServerErrors;
import com.br3akPoint.auth_service.entity.ClientDevice;
import com.br3akPoint.auth_service.repository.ClientDeviceRepository;
import com.br3akPoint.error.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientDeviceService {
    @Autowired
    private ClientDeviceRepository clientDeviceRepository;

    public boolean isSeeded() {
        return clientDeviceRepository.count() > 0;
    }

    public void seedDevices(List<ClientDevice> clientDevices) {
        clientDeviceRepository.saveAll(clientDevices);
    }

    public ClientDevice getByAppId(String appId) throws Exception {
        return clientDeviceRepository.findByAppId(appId)
                .orElseThrow(() -> BusinessException.badRequest(ServerErrors.Invalid_App_ID));
    }
}
