package com.touplus.billing_message.event;

import com.touplus.billing_message.service.MessageDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageEventListener {

    private final MessageDispatchService messageDispatchService;

    @Async
    @EventListener
    public void handleMessageReadyEvent(MessageReadyEvent event) {
        log.info("이벤트 수신: 메시지 {}건 발송 트리거", event.getMessageCount());
        messageDispatchService.dispatchAllWaitedMessages();
    }
}
