package com.br3akPoint.auth_service.data;

import com.br3akPoint.auth_service.constant.DeviceTypeEnum;
import com.br3akPoint.auth_service.entity.ClientDevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class Platform {
    private final Environment environment;
    private Platform(@Autowired Environment environment) {
        this.environment = environment;
    }

    public List<ClientDevice> getPlatforms() {
        return List.of(
                ClientDevice.builder()
                        .deviceType(DeviceTypeEnum.android)
                        .appId(environment.getProperty("app.platforms.android.app-id"))
                        .appSecret(environment.getProperty("app.platforms.android.secret"))
                        .build(),

                ClientDevice.builder()
                        .deviceType(DeviceTypeEnum.iOS)
                        .appId(environment.getProperty("app.platforms.ios.app-id"))
                        .appSecret(environment.getProperty("app.platforms.ios.secret"))
                        .build(),

                ClientDevice.builder()
                        .deviceType(DeviceTypeEnum.web)
                        .appId(environment.getProperty("app.platforms.web.app-id"))
                        .appSecret(environment.getProperty("app.platforms.web.secret"))
                        .build(),

                ClientDevice.builder()
                        .deviceType(DeviceTypeEnum.desktop)
                        .appId(environment.getProperty("app.platforms.desktop.app-id"))
                        .appSecret(environment.getProperty("app.platforms.desktop.secret"))
                        .build(),

                ClientDevice.builder()
                        .deviceType(DeviceTypeEnum.macOS)
                        .appId(environment.getProperty("app.platforms.macOS.app-id"))
                        .appSecret(environment.getProperty("app.platforms.macOS.secret"))
                        .build()
        );
    }
}
