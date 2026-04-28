package com.br3akPoint.auth_service.util;

import com.br3akPoint.auth_service.constant.ServerErrors;
import com.br3akPoint.auth_service.entity.ClientDevice;
import com.br3akPoint.auth_service.service.ClientDeviceService;
import com.br3akPoint.error.BusinessException;
import com.br3akPoint.util.AesEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class DeviceClientValidator {

    @Value("${MAX_SIGNATURE_AGE_SECONDS}")
    private long signatureExpiry;

    private final ClientDeviceService clientService;

    @Autowired
    public DeviceClientValidator(ClientDeviceService clientDeviceService) {
        clientService = clientDeviceService;
    }

    public String validate(String requestAppId, String requestSignature) throws Exception {
        // Fetch client device from DB using app_id from request header
        ClientDevice clientDevice = clientService.getByAppId(requestAppId);
        String appSecret = clientDevice.getAppSecret();

        // Decrypt the signature using app secret fetched from DB
        String decryptedPayload;
        try {
            decryptedPayload = AesEncryptionUtil.decrypt(requestSignature, appSecret);
        } catch (RuntimeException e) {
            throw BusinessException.badRequest(ServerErrors.Invalid_Signatures);
        }

        // Parse decrypted payload into key-value pairs
        // Expected format: now=<timestamp>&expiry=<timestamp>&app_id=<app_id>&nonce=<uuid>
        Map<String, String> params = parsePayload(decryptedPayload);

        String payloadAppId = params.get("app_id");
        String nowStr       = params.get("now");
        String expiryStr    = params.get("expiry");
        String nonce        = params.get("nonce");

        // Ensure all required fields are present in the payload
        if (payloadAppId == null || nowStr == null || expiryStr == null || nonce == null) {
            throw BusinessException.badRequest(ServerErrors.Invalid_Signatures);
        }

        // Verify app_id in payload matches the one in request header
        if (!clientDevice.getAppId().equals(payloadAppId)) {
            throw BusinessException.badRequest(ServerErrors.Invalid_Signatures);
        }

        // Parse timestamps from payload
        long now, expiry;
        try {
            now    = Long.parseLong(nowStr);
            expiry = Long.parseLong(expiryStr);
        } catch (NumberFormatException e) {
            throw BusinessException.badRequest(ServerErrors.Invalid_Signatures);
        }

        long currentTime = Instant.now().getEpochSecond();

        // Reject if 'now' is too far in the future — allows 30s clock skew
        if (now > currentTime + 30) {
            throw BusinessException.badRequest(ServerErrors.Invalid_Signatures);
        }

        // Reject if expiry window exceeds the allowed maximum signature age
        if ((expiry - now) > signatureExpiry) {
            throw BusinessException.badRequest(ServerErrors.Signature_Expired);
        }

        // Reject if signature has already expired
        if (currentTime > expiry) {
            throw BusinessException.badRequest(ServerErrors.Signature_Expired);
        }

        return clientDevice.getDeviceType().name();
    }

    /**
     * Parses a query-string style payload into a key-value map.
     * Example input: "now=123&expiry=456&app_id=abc&nonce=uuid"
     */
    private Map<String, String> parsePayload(String payload) {
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