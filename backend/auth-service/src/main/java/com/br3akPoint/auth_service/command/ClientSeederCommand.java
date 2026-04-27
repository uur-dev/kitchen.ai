package com.br3akPoint.auth_service.command;

import com.br3akPoint.auth_service.data.Platform;
import com.br3akPoint.auth_service.service.ClientDeviceService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ClientSeederCommand implements ApplicationListener<ApplicationReadyEvent> {

    private final ClientDeviceService clientDeviceService;
    private final Platform platform;

    @Autowired
    public ClientSeederCommand(ClientDeviceService clientDeviceService, Platform platform) {
        this.clientDeviceService = clientDeviceService;
        this.platform = platform;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        seedDevices();
    }

    private void seedDevices() {
        if(!clientDeviceService.isSeeded()) {
            clientDeviceService.seedDevices(platform.getPlatforms());
        }
    }
}
