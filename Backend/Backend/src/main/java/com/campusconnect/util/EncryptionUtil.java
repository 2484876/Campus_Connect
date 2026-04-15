package com.campusconnect.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptionUtil(@Value("${app.encryption.key}") String base64Key) {
        if (base64Key == null || base64Key.isBlank() || base64Key.startsWith("${")) {
            throw new IllegalStateException(
                    "Encryption key not set. Set APP_ENCRYPTION_KEY environment variable"
            );
        }
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("Encryption key must be 256 bits (32 bytes)");
        }
        this.keySpec = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));

            ByteBuffer buffer = ByteBuffer.allocate(IV_LENGTH + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String cipherText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public boolean isEncrypted(String text) {
        try {
            byte[] decoded = Base64.getDecoder().decode(text);
            return decoded.length > IV_LENGTH;
        } catch (Exception e) {
            return false;
        }
    }
}