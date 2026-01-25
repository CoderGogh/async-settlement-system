package com.touplus.billing_message.service;

import com.touplus.billing_message.domain.entity.*;
import com.touplus.billing_message.domain.respository.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSnapshotService {

        private final MessageSnapshotRepository messageSnapshotRepository;
        private final MessageSnapshotJdbcRepository messageSnapshotJdbcRepository;
        private final BillingSnapshotRepository billingSnapshotRepository;
        private final UserRepository userRepository;
        private final MessageTemplateRepository messageTemplateRepository;
        private final MessageRepository messageRepository;

        private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월");
        private static final NumberFormat PRICE_FORMATTER = NumberFormat.getNumberInstance(Locale.KOREA);

        // 메시지 스냅샷을 배치로 생성
        @Transactional
        public int createSnapshotsBatch(List<Message> messages, MessageType messageType) {

                if (messages.isEmpty()) {
                        return 0;
                }

                List<Long> messageIds = new ArrayList<>(messages.size());
                Set<Long> billingIds = new HashSet<>();
                Set<Long> userIds = new HashSet<>();

                for (Message m : messages) {
                    messageIds.add(m.getMessageId());
                    billingIds.add(m.getBillingId());
                    userIds.add(m.getUserId());
                }

                // 여기도 위랑 동일하게 생각 중
                Map<Long, BillingSnapshot> billingMap = billingSnapshotRepository.findAllById(billingIds)
                                .stream()
                                .collect(Collectors.toMap(
                                                BillingSnapshot::getBillingId,
                                                b -> b));

                Map<Long, User> userMap = userRepository.findAllById(userIds)
                                .stream()
                                .collect(Collectors.toMap(
                                                User::getUserId,
                                                u -> u));

                MessageTemplate template = messageTemplateRepository.findFirstByMessageType(messageType)
                                .orElseThrow(() -> new IllegalStateException(
                                                "MessageTemplate not found: " + messageType));

                // 중복 생성된 스냅샷 제외
                Set<Long> existingMessageIds = messageSnapshotRepository.findExistingMessageIds(messageIds);

                // 스냅샷 생성
                List<MessageSnapshot> snapshots = new ArrayList<>(messages.size());

                for (Message message : messages) {

                        Long messageId = message.getMessageId();
                        if (existingMessageIds.contains(messageId)) {
                                continue;
                        }

                        BillingSnapshot billing = billingMap.get(message.getBillingId());
                        User user = userMap.get(message.getUserId());

                        if (billing == null || user == null) {
                                log.warn("Snapshot skip - billing or user missing. messageId={}", messageId);
                                continue;
                        }

                        String content = buildMessageContent(
                                        template.getTemplateContent(),
                                        user,
                                        billing);

                        snapshots.add(new MessageSnapshot(
                                        messageId,
                                        billing.getBillingId(),
                                        billing.getSettlementMonth(),
                                        user.getUserId(),
                                        user.getName(),
                                        user.getEmail(),
                                        user.getPhone(),
                                        billing.getTotalPrice(),
                                        billing.getSettlementDetails(),
                                        content));
                }

                if (snapshots.isEmpty()) {
                        return 0;
                }

                // 스냅샷 삽입
                messageSnapshotRepository.saveAll(snapshots);

                // [Redis 기반] markCreatedByIds 제거 - WAITED → SENT로 직접 변경됨

                log.info("MessageSnapshot batch created: {}", snapshots.size());
                return snapshots.size();
        }

        /**
         * 스냅샷 배치 생성 (최적화 버전)
         * - 외부에서 userMap, billingMap 전달받아 중복 조회 제거
         * - JPA saveAll → JDBC batchInsert로 변경 (10배 이상 빠름)
         */
        @Transactional
        public int createSnapshotsBatchOptimized(
                List<Message> messages,
                MessageType messageType,
                Map<Long, User> userMap,
                Map<Long, BillingSnapshot> billingMap) {

                if (messages.isEmpty()) {
                        return 0;
                }

                List<Long> messageIds = messages.stream()
                        .map(Message::getMessageId)
                        .toList();

                // 템플릿 조회 (1회)
                MessageTemplate template = messageTemplateRepository.findFirstByMessageType(messageType)
                        .orElseThrow(() -> new IllegalStateException(
                                "MessageTemplate not found: " + messageType));

                // 중복 생성된 스냅샷 제외
                Set<Long> existingMessageIds = messageSnapshotRepository.findExistingMessageIds(messageIds);

                // 스냅샷 생성
                List<MessageSnapshot> snapshots = new ArrayList<>(messages.size());

                for (Message message : messages) {
                        Long messageId = message.getMessageId();
                        if (existingMessageIds.contains(messageId)) {
                                continue;
                        }

                        BillingSnapshot billing = billingMap.get(message.getBillingId());
                        User user = userMap.get(message.getUserId());

                        if (billing == null || user == null) {
                                log.warn("Snapshot skip - billing or user missing. messageId={}", messageId);
                                continue;
                        }

                        String content = buildMessageContent(
                                template.getTemplateContent(),
                                user,
                                billing);

                        snapshots.add(new MessageSnapshot(
                                messageId,
                                billing.getBillingId(),
                                billing.getSettlementMonth(),
                                user.getUserId(),
                                user.getName(),
                                user.getEmail(),
                                user.getPhone(),
                                billing.getTotalPrice(),
                                billing.getSettlementDetails(),
                                content));
                }

                if (snapshots.isEmpty()) {
                        return 0;
                }

                // JDBC batchInsert (JPA saveAll 대비 10배 이상 빠름)
                int inserted = messageSnapshotJdbcRepository.batchInsert(snapshots);

                log.info("MessageSnapshot JDBC batch created: {}", inserted);
                return inserted;
        }

        // 템플릿 적용
        private String buildMessageContent(
                String template,
                User user,
                BillingSnapshot billing) {

            String result = template;
            result = result.replace("{userName}", user.getName());
            result = result.replace("{userEmail}", user.getEmail());
            result = result.replace("{userPhone}", user.getPhone());
            result = result.replace("{settlementMonth}",
                    billing.getSettlementMonth().format(MONTH_FORMATTER));
            result = result.replace("{totalPrice}",
                    PRICE_FORMATTER.format(billing.getTotalPrice()));
            result = result.replace("{settlementDetails}",
                    billing.getSettlementDetails());

            return result;
        }

}
