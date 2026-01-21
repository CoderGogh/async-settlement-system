package com.touplus.billing_api.common.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
/*
  설정 파일 필수로 넣어줘야함!!!
* */
@Component
public class Decrypto {

    private final String secretKey;
    private final String iv;

    public Decrypto(
            @Value("${crypto.aes.secret-key}") String secretKey,
            @Value("${crypto.aes.iv}") String iv
    ) {
        this.secretKey = secretKey;
        this.iv = iv;
    }

    public String decrypt(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            return encryptedValue;
        }
        return CryptoUtils.decryptAES(encryptedValue, secretKey, iv);
    }
}
