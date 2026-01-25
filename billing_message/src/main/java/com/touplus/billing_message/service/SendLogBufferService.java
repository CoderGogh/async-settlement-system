package com.touplus.billing_message.service;

import com.touplus.billing_message.domain.entity.MessageType;
import com.touplus.billing_message.domain.respository.MessageSendLogJdbcRepository;
import com.touplus.billing_message.domain.respository.MessageSendLogJdbcRepository.SendLogDto;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendLogBufferService {

    private final MessageSendLogJdbcRepository messageSendLogJdbcRepository;
    private final ConcurrentLinkedQueue<SendLogDto> logQueue = new ConcurrentLinkedQueue<>();

    /**
     * 로그를 큐에 추가 (DB 호출 없음, 즉시 반환)
     */
    public void addLog(Long messageId, int retryNo, MessageType messageType,
                       String responseCode, String responseMessage, LocalDateTime sentAt) {
        logQueue.offer(new SendLogDto(messageId, retryNo, messageType, responseCode, responseMessage, sentAt));
    }

    /**
     * 주기적으로 큐의 로그를 Bulk INSERT (500ms마다)
     */
    @Scheduled(fixedRate = 500)
    public void flushLogs() {
        List<SendLogDto> batch = drainQueue();
        if (batch.isEmpty()) return;

        try {
            messageSendLogJdbcRepository.bulkInsert(batch);
            log.debug("SendLog Bulk INSERT 완료: {}건", batch.size());
        } catch (Exception e) {
            log.error("SendLog Bulk INSERT 실패: {}건", batch.size(), e);
        }
    }

    /**
     * 큐에서 모든 로그를 꺼냄
     */
    private List<SendLogDto> drainQueue() {
        List<SendLogDto> batch = new ArrayList<>();
        SendLogDto log;
        while ((log = logQueue.poll()) != null) {
            batch.add(log);
        }
        return batch;
    }

    /**
     * 애플리케이션 종료 시 남은 로그 처리
     */
    @PreDestroy
    public void shutdown() {
        log.info("SendLogBufferService 종료 - 남은 로그 처리 시작");
        List<SendLogDto> remaining = drainQueue();
        if (!remaining.isEmpty()) {
            try {
                messageSendLogJdbcRepository.bulkInsert(remaining);
                log.info("종료 시 SendLog 저장 완료: {}건", remaining.size());
            } catch (Exception e) {
                log.error("종료 시 SendLog 저장 실패: {}건", remaining.size(), e);
            }
        }
    }

    /**
     * 현재 큐 사이즈 (모니터링용)
     */
    public int getQueueSize() {
        return logQueue.size();
    }
}
