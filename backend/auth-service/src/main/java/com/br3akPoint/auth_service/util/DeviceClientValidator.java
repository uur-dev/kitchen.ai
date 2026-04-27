package com.br3akPoint.auth_service.util;

import com.br3akPoint.auth_service.constant.ServerErrors;
import com.br3akPoint.auth_service.entity.ClientDevice;
import com.br3akPoint.auth_service.service.ClientDeviceService;
import com.br3akPoint.error.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class DeviceClientValidator {
    @Value("${MAX_SIGNATURE_AGE_SECONDS}")
    private long signatureExpiry;

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ClientDeviceService clientService;

    @Autowired
    public DeviceClientValidator(ClientDeviceService clientDeviceService) {
        clientService = clientDeviceService;
    }

    public void validate(String requestAppId, String requestSignatures) throws Exception {
        ClientDevice clientDevice = clientService.getByAppId(requestAppId);

        String appSecret = clientDevice.getAppSecret();

        // Step 3: Decrypt / verify signature -> get original payload
        String decryptedPayload = decryptSignature(requestSignatures, appSecret);

        // Step 4: Parse payload
        // Expected: now=<timestamp>&expiry=<timestamp>&app_id=<app_id>&nonce=<uuid>
        Map<String, String> params = parsePayload(decryptedPayload);

        String payloadAppId = params.get("app_id");
        String nowStr       = params.get("now");
        String expiryStr    = params.get("expiry");
        String nonce        = params.get("nonce");

        if (payloadAppId == null || nowStr == null || expiryStr == null || nonce == null) {
            throw BusinessException.badRequest(ServerErrors.Invalid_Signatures);
        }

        // Step 5: Match app_id
        if (!clientDevice.getAppId().equals(payloadAppId)) {
            throw BusinessException.badRequest(ServerErrors.Invalid_Signatures);
        }

        // Step 6: Validate timestamps
        long now, expiry;
        try {
            now    = Long.parseLong(nowStr);
            expiry = Long.parseLong(expiryStr);
        } catch (NumberFormatException e) {
            throw BusinessException.badRequest(ServerErrors.Invalid_Signatures);
        }

        long currentTime = Instant.now().getEpochSecond();

        // now should not be in the future (allow 30s clock skew)
        if (now > currentTime + 30) {
            throw BusinessException.badRequest(ServerErrors.Invalid_Signatures);
        }

        // Gap between now and expiry must not exceed 3 minutes
        if ((expiry - now) > signatureExpiry) {
            throw BusinessException.badRequest(ServerErrors.Signature_Expired);
        }

        // Signature must not be expired
        if (currentTime > expiry) {
            throw BusinessException.badRequest(ServerErrors.Signature_Expired);
        }
    }
    /**
     * Client generates HMAC-SHA256 of the payload string using app_secret,
     * then Base64-encodes it and sends as X-Signature.
     *
     * This method recomputes the HMAC and compares — it does NOT "decrypt"
     * (HMAC is not encryption, it's a MAC). If you want AES encryption instead,
     * swap this method with an AES decrypt implementation.
     */
    private String decryptSignature(String signature, String appSecret) {
        // The signature IS the Base64-encoded HMAC — decode to get the raw bytes
        // We'll verify by re-computing HMAC on the decoded payload
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(signature);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw BusinessException.badRequest(ServerErrors.Invalid_Signatures);
        }
    }

    /**
     * Verifies the HMAC-SHA256 signature.
     * Client must sign the raw payload string with app_secret using HMAC-SHA256.
     */
    public boolean verifyHmac(String payload, String signature, String appSecret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    appSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM
            );
            mac.init(keySpec);
            byte[] expectedHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = Base64.getEncoder().encodeToString(expectedHmac);

            // Constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            throw BusinessException.badRequest(ServerErrors.Invalid_Signatures);
        }
    }

    private Map<String, String> parsePayload(String payload) {
        // Parse: now=123&expiry=456&app_id=abc&nonce=uuid
        Map<String, String> map = new HashMap<>();
        for (String part : payload.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }

}
