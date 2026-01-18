package com.touplus.billing_message.consumer;

import com.touplus.billing_message.domain.dto.BillingResultMessage;
import com.touplus.billing_message.domain.entity.BillingSnapshot;
import com.touplus.billing_message.domain.respository.BillingSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class BillingResultConsumer {

    private final BillingSnapshotRepository billingSnapshotRepository;

    @KafkaListener(
        topics = "billing-result",
        groupId = "billing-message-group"
    )
    @Transactional
    public void consume(BillingResultMessage message) {

        try {
            BillingSnapshot snapshot = new BillingSnapshot(
                    message.getId(),
                    message.getSettlementMonth(),
                    message.getUserId(),
                    message.getTotalPrice(),
                    message.getSettlementDetails() != null
                            ? message.getSettlementDetails().toString() : "{}"
            );

            billingSnapshotRepository.save(snapshot);
            log.info("billing_snapshot 저장 완료 billingId={}", snapshot.getBillingId());

        } catch (DataIntegrityViolationException e) {
            log.info("중복 Kafka 메시지 무시");
        }
    }

    // Map에서 Long 가져오기
    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) throw new IllegalArgumentException("필수 Long 값이 없음: " + key);
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Long) return (Long) value;
        return Long.valueOf(value.toString());
    }

    // Map에서 Integer 가져오기
    private Integer getInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) throw new IllegalArgumentException("필수 Integer 값이 없음: " + key);
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        return Integer.valueOf(value.toString());
    }

    // Map에서 LocalDate 가져오기 (settlementMonth: [2025,12,1])
    private LocalDate getLocalDate(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            int year = ((Number) list.get(0)).intValue();
            int month = ((Number) list.get(1)).intValue();
            int day = ((Number) list.get(2)).intValue();
            return LocalDate.of(year, month, day);
        }
        throw new IllegalArgumentException("필수 LocalDate 값이 없음: " + key);
    }
}
