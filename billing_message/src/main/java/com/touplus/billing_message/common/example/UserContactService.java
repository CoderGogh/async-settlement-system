package com.touplus.billing_message.common.example;


import org.springframework.stereotype.Service;

import com.touplus.billing_message.common.crypto.Decrypto;
import com.touplus.billing_message.common.masking.MaskingUtils;
import com.touplus.billing_message.domain.dto.UserContactDto;
import com.touplus.billing_message.domain.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserContactService {

    private final Decrypto decrypto;

    public UserContactDto decryptAndMask(User user) {
        if (user == null) {
            return null;
        }

        return UserContactDto.builder()
                .userId(user.getUserId())
                .name(
                        MaskingUtils.maskName(
                                user.getName()
                        )
                )
                .email(
                        MaskingUtils.maskEmail(
                                decrypto.decrypt(user.getEmail())
                        )
                )
                .phone(
                        MaskingUtils.maskPhone(
                                decrypto.decrypt(user.getPhone())
                        )
                )
                .build();
    }
}
