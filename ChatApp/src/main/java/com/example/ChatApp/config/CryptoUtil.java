package com.example.ChatApp.config;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CryptoUtil {

    private static final String SECRET = "1234567890123456"; // 16 chars = AES-128
    private static final String ALGO = "AES";

    private static SecretKeySpec key() {
        return new SecretKeySpec(SECRET.getBytes(), ALGO);
    }

    public static String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key());
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static String decrypt(String cipherText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key());
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return new String(cipher.doFinal(decoded));
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
