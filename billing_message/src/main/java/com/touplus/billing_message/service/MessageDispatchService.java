package com.touplus.billing_message.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.touplus.billing_message.domain.entity.Message;
import com.touplus.billing_message.domain.entity.MessageType;

import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageDispatchService {

    private final MessageClaimService messageClaimService;
    private final MessageProcessService messageProcessService;
    private final TaskExecutor messageDispatchTaskExecutor;
    private final MessageSnapshotService messageSnapshotService;

    public MessageDispatchService(
            MessageClaimService messageClaimService,
            MessageProcessService messageProcessService,
            MessageSnapshotService messageSnapshotService,
            @Qualifier("messageDispatchTaskExecutor")
            TaskExecutor messageTaskExecutor
    ) {
        this.messageClaimService = messageClaimService;
        this.messageProcessService = messageProcessService;
        this.messageSnapshotService = messageSnapshotService;
        this.messageDispatchTaskExecutor = messageTaskExecutor;
    }

   /* 미수 : 이것저것 주석 처리하고 파란색 주석 없는 건 다 내가 추가한 거 -> 배치로 빨리 돌리려고
    	public void dispatchDueMessages() {
        List<Long> messageIds = messageClaimService.claimNextMessages(LocalDateTime.now());
        if (messageIds.isEmpty()) {
            log.debug("발송 대상 메시지 없음");
            return;
        }

        log.info("메시지 {}건 발송 시작", messageIds.size());
        for (Long messageId : messageIds) {
            messageDispatchTaskExecutor.execute(() -> processWithExceptionHandling(messageId));
        }
    }*/
  
    /**
     * 예외 처리를 포함한 메시지 처리
     */
    private void processWithExceptionHandling(Long messageId) {
        try {
            messageProcessService.processMessage(messageId);
        } catch (Exception e) {
            log.error("메시지 처리 중 예기치 않은 오류 messageId={}", messageId, e);
            // 예외 발생 시 메시지를 WAITED 상태로 되돌려 재처리 가능하게 함
            try {
                messageProcessService.handleSendFailure(messageId, 0, null);
            } catch (Exception recoveryEx) {
                log.error("메시지 복구 실패 messageId={}", messageId, recoveryEx);
            }
        }
    }

    /**
     * 스케줄 무시하고 WAITED 상태의 메시지 발송 (테스트용)
     * 한 번의 배치만 처리 (최대 40건)
     * 
     * @return 발송 시작한 메시지 수
     */
   /* public int dispatchAllWaited() {
        List<Long> messageIds = messageClaimService.claimNextMessagesIgnoreSchedule();
        if (messageIds.isEmpty()) {
            log.info("발송 대상 메시지 없음");
            return 0;
        }

        log.info("메시지 {}건 발송 시작 (스케줄 무시)", messageIds.size());
        for (Long messageId : messageIds) {
            messageDispatchTaskExecutor.execute(() -> processWithExceptionHandling(messageId));
        }

        log.info("총 발송 시작: {}건", messageIds.size());
        return messageIds.size();
    }*/
    
    // 미수 : 배치 때문에 추가된 코드 
    @Transactional
    public List<Long> prepareDispatch(LocalDateTime now) {

        List<Message> messages =
            messageClaimService.claimNextMessagesAsEntities(now);

        if (messages.isEmpty()) {
            return List.of();
        }

        messageSnapshotService.createSnapshotsBatch(messages, MessageType.EMAIL);

        return messages.stream()
                .map(Message::getMessageId)
                .toList();
    }

    public void dispatchPreparedMessages(List<Long> messageIds) {

        for (Long messageId : messageIds) {
            messageDispatchTaskExecutor.execute(
                () -> processWithExceptionHandling(messageId)
            );
        }
    }

    public void dispatchDueMessages() {

        List<Long> messageIds =
            prepareDispatch(LocalDateTime.now());

        if (messageIds.isEmpty()) {
            log.debug("발송 대상 메시지 없음");
            return;
        }

        log.info("메시지 {}건 dispatch 시작", messageIds.size());
        dispatchPreparedMessages(messageIds);
    }

    
    /*public int dispatchAllWaited() {
        List<Long> messageIds = messageClaimService.claimNextMessages(LocalDateTime.now());
        
        if (messageIds.isEmpty()) {
            log.info("발송 대상 메시지 없음");
            return 0;
        }

        log.info("메시지 {}건 발송 시작 - WAITED만", messageIds.size());
        for (Long messageId : messageIds) {
            messageDispatchTaskExecutor.execute(() -> processWithExceptionHandling(messageId));
        }

        return messageIds.size();
    }*/
}
