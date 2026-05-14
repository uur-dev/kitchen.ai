package com.br3akPoint.auth_service;

import com.br3akPoint.auth_service.repository.ClientDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import util.AesEncryptionUtil;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
		properties = {
				"spring.cloud.config.enabled=false",
				"spring.cloud.discovery.enabled=false",
				"spring.data.redis.repositories.enabled=false",
				"spring.flyway.enabled=false"
		}
)
class AuthServiceApplicationTests {

	@Autowired
	private ClientDeviceRepository clientDeviceRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void generateSignatures() {
		String appId = "<any-client-id>";
		var client = clientDeviceRepository.findByAppId(appId).orElseThrow();

		String appSecret = client.getAppSecret();

		long now = Instant.now().getEpochSecond();
		long expiry = now + 3600; // 1 hour from now
		String deviceId = UUID.randomUUID().toString();
		String nonce = UUID.randomUUID().toString();

		String payload = String.format("now=%d&expiry=%d&app_id=%s&device_id=%s&nonce=%s",
				now, expiry, appId, deviceId, nonce);

		String signature = AesEncryptionUtil.encrypt(payload, appSecret);

		System.out.println("Generated Signature: " + signature);
		assertNotNull(signature);
	}

}
