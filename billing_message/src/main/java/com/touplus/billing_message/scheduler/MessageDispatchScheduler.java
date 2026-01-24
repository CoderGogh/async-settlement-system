package com.touplus.billing_message.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.touplus.billing_message.service.MessageDispatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MessageDispatchScheduler
 * 
 * 백업 폴링: 매시 정각에 누락된 WAITED 메시지 전체 처리
 * (메인 발송은 MessageProcessor에서 직접 호출)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageDispatchScheduler {

    private final MessageDispatchService messageDispatchService;

    /**
     * 백업 스케줄러 - 매시 정각에 실행
     * 이벤트 유실이나 서버 재시작으로 누락된 WAITED 메시지를 전체 조회하여 처리
     */
    @Scheduled(cron = "0 0 * * * *")
    public void backupDispatch() {
        log.info("[Backup Scheduler] 매시 정각 백업 폴링 시작");
        
        try {
            int processed = messageDispatchService.dispatchAllWaitedMessages();
            log.info("[Backup Scheduler] 백업 폴링 완료: {}건 처리", processed);
        } catch (Exception e) {
            log.error("[Backup Scheduler] 백업 폴링 실패", e);
        }
    }
}
