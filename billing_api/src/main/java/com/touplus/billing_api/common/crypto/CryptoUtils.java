package com.touplus.billing_api.common.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class CryptoUtils {

    // 알고리즘/모드/패딩 명시
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM = "AES";
    private static final int IV_LENGTH = 16;

    private CryptoUtils() {}

    /**
     * AES 암호화
     *
     * @param plainText 암호화할 문자열
     * @param secretKey 16 / 24 / 32 바이트 키
     * @param iv 16바이트 초기화 벡터
     */
    public static String encryptAES(String plainText, String secretKey, String iv) {
        validate(secretKey, iv);

        try {
            SecretKeySpec keySpec =
                    new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            IvParameterSpec ivSpec =
                    new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] encrypted =
                    cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES 암호화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * AES 복호화
     *
     * @param encrypted Base64 인코딩된 문자열
     * @param secretKey 16 / 24 / 32 바이트 키
     * @param iv 16바이트 초기화 벡터
     */
    public static String decryptAES(String encrypted, String secretKey, String iv) {
        validate(secretKey, iv);

        try {
            SecretKeySpec keySpec =
                    new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            IvParameterSpec ivSpec =
                    new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decodedValue = Base64.getDecoder().decode(encrypted);
            byte[] decrypted = cipher.doFinal(decodedValue);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES 복호화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 키 / IV 검증
     */
    private static void validate(String secretKey, String iv) {
        int keyLength = secretKey.getBytes(StandardCharsets.UTF_8).length;
        if (keyLength != 16 && keyLength != 24 && keyLength != 32) {
            throw new IllegalArgumentException("AES 키 길이는 16, 24, 32바이트여야 합니다.");
        }

        if (iv.getBytes(StandardCharsets.UTF_8).length != IV_LENGTH) {
            throw new IllegalArgumentException("IV 길이는 16바이트여야 합니다.");
        }
    }
}