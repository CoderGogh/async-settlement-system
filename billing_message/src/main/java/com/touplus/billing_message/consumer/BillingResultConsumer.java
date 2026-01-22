package com.touplus.billing_message.consumer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.touplus.billing_message.domain.dto.BillingResultDto;
import com.touplus.billing_message.domain.entity.BillingSnapshot;
import com.touplus.billing_message.domain.respository.BillingSnapshotJdbcRepository;
import com.touplus.billing_message.domain.respository.BillingSnapshotRepository;
import com.touplus.billing_message.processor.MessageProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingResultConsumer {

    private final BillingSnapshotJdbcRepository jdbcRepository;
    private final BillingSnapshotRepository bsr;
    private final MessageProcessor messageProcessor;

    @KafkaListener(topics = "billing-result-topic-2512", groupId = "billing-message-group1", containerFactory = "kafkaListenerContainerFactory")
    public void consume(
            List<BillingResultDto> messages,
            Acknowledgment ack) {
        try {
            LocalDate now = LocalDate.now();
            log.info("데이터 넣기 시작 시각 : {}", LocalDateTime.now());

            List<BillingSnapshot> toUpsert = new ArrayList<>();

            for (BillingResultDto message : messages) {
                LocalDate settlementMonth = message.getSettlementMonth();

                if (settlementMonth == null)
                    continue;

                LocalDate processMonth = settlementMonth.plusMonths(1);
                if (processMonth.getYear() != now.getYear()
                        || processMonth.getMonth() != now.getMonth()) {
                    continue;
                }

                toUpsert.add(new BillingSnapshot(
                        message.getId(),
                        settlementMonth,
                        message.getUserId(),
                        message.getTotalPrice(),
                        message.getSettlementDetails() != null
                                ? message.getSettlementDetails().toString()
                                : "{}"));
            }

            int batchSize = 1000;

            for (int i = 0; i < toUpsert.size(); i += batchSize) {
                int end = Math.min(i + batchSize, toUpsert.size());
                jdbcRepository.batchUpsertByUserMonth(toUpsert.subList(i, end));
            }

            Long snapshotCount = bsr.countAll();
            if (snapshotCount >= 9999L) {
                log.info("스냅샷 데이터 다 넣음! count={}", snapshotCount);
                log.info("Message 처리 시작 시각 : {}", LocalDateTime.now());

                int messageBatchSize = 1000;
                int parallelCount = 4;

                List<List<BillingSnapshot>> chunks = new ArrayList<>();
                int pageNum = 0;
                Page<BillingSnapshot> page;
                do {
                    page = bsr.findAll(PageRequest.of(pageNum++, messageBatchSize));
                    if (!page.getContent().isEmpty()) {
                        chunks.add(new ArrayList<>(page.getContent()));
                    }
                } while (page.hasNext());

                java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors
                        .newFixedThreadPool(parallelCount);

                List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
                for (List<BillingSnapshot> chunk : chunks) {
                    futures.add(executor.submit(() -> messageProcessor.processBatch(chunk)));
                }

                for (java.util.concurrent.Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (Exception e) {
                        log.error("청크 처리 실패", e);
                    }
                }

                executor.shutdown();
                log.info("Message 처리 완료 시각 : {}", LocalDateTime.now());
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Kafka batch 처리 실패", e);
        }
    }
}
