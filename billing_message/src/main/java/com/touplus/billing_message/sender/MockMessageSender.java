package com.touplus.billing_message.sender;

import com.touplus.billing_message.common.crypto.Decrypto;
import com.touplus.billing_message.common.masking.MaskingUtils;
import com.touplus.billing_message.domain.entity.MessageSnapshot;
import com.touplus.billing_message.domain.entity.MessageType;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockMessageSender implements MessageSender {

    private final Decrypto decrypto;

    @Override
    public SendResult send(MessageType type, MessageSnapshot snapshot) {
        // 복호화
        String decryptedEmail = decrypto.decrypt(snapshot.getUserEmail());
        String decryptedPhone = decrypto.decrypt(snapshot.getUserPhone());

        // 마스킹 (로그용)
        String maskedEmail = MaskingUtils.maskEmail(decryptedEmail);
        String maskedPhone = MaskingUtils.maskPhone(decryptedPhone);

        if (type == MessageType.SMS) {
            log.debug("SMS 발송 완료: phone={}", maskedPhone);
            return SendResult.ok("OK", maskedPhone);
        }

        // EMAIL: 1% 확률로 실패
        boolean failed = ThreadLocalRandom.current().nextInt(100) == 0;
        if (failed) {
            log.warn("EMAIL 발송 실패: email={}", maskedEmail);
            return SendResult.fail("MOCK_FAIL", maskedEmail);
        }

        log.debug("EMAIL 발송 완료: email={}", maskedEmail);
        return SendResult.ok("OK", maskedEmail);
    }
}
