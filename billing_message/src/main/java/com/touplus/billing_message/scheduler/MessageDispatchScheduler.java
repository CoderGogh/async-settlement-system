package com.touplus.billing_message.scheduler;

import com.touplus.billing_message.domain.entity.Message;
import com.touplus.billing_message.domain.entity.MessageStatus;
import com.touplus.billing_message.domain.respository.MessageRepository;
import com.touplus.billing_message.service.MessageDispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageDispatchScheduler {

    private final MessageDispatchService messageDispatchService;
    private final MessageRepository messageRepository;
    
    @Scheduled(fixedDelayString = "${message.dispatch.poll-delay-ms:2000}")
    public void dispatch() {
    	
    	// 미수 : WAITED가 없으면 비활성화 - 스케줄 자체를 종료하는 건 아님 -> 찾아보니까 원래 이렇게 쓰는 거라던데...?
    	if (!messageRepository.existsByStatus(MessageStatus.WAITED)) {
    		System.out.println("WAITED X");
            return;
        }
    	
    	messageDispatchService.dispatchDueMessages();
    	System.out.println("WAITED O");
    }
}
