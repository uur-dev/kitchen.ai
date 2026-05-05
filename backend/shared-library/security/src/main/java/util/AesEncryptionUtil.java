package util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class AesEncryptionUtil {

    private static final String AES_ALGORITHM       = "AES";
    private static final String AES_TRANSFORMATION  = "AES/CBC/PKCS5Padding";
    private static final int    IV_SIZE             = 16;
    private static final int    KEY_SIZE            = 16; // AES-128

    /**
     * Encrypts a plain text string using AES/CBC/PKCS5Padding.
     *
     * Steps:
     *   1. Generate a random 16-byte IV
     *   2. Encrypt the payload using AES/CBC with the first 16 bytes of the secret key
     *   3. Concatenate IV + cipherText
     *   4. Base64 encode and return
     */
    public static String encrypt(String plainText, String secretKey) {
        try {
            byte[] keyBytes    = Arrays.copyOf(secretKey.getBytes(StandardCharsets.UTF_8), KEY_SIZE);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);

            // Generate random IV
            byte[] iv = new byte[IV_SIZE];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine IV + cipherText -> Base64
            byte[] combined = new byte[IV_SIZE + cipherText.length];
            System.arraycopy(iv,         0, combined, 0,       IV_SIZE);
            System.arraycopy(cipherText, 0, combined, IV_SIZE, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts a Base64 encoded AES/CBC/PKCS5Padding encrypted string.
     *
     * Steps:
     *   1. Base64 decode the input
     *   2. Extract first 16 bytes as IV, remaining as cipherText
     *   3. Decrypt using AES/CBC with the first 16 bytes of the secret key
     *   4. Return the original plain text
     */
    public static String decrypt(String encryptedText, String secretKey) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);

            // First 16 bytes are IV, rest is cipherText
            byte[] iv         = Arrays.copyOfRange(encryptedBytes, 0,       IV_SIZE);
            byte[] cipherText = Arrays.copyOfRange(encryptedBytes, IV_SIZE, encryptedBytes.length);

            byte[] keyBytes = Arrays.copyOf(secretKey.getBytes(StandardCharsets.UTF_8), KEY_SIZE);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decryptedBytes = cipher.doFinal(cipherText);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid Base64 signature", e);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}