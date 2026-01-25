package com.touplus.billing_message.consumer;

import com.touplus.billing_message.service.DispatchActivationFlag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchSignalListener {

    private final KafkaListenerEndpointRegistry registry;
    private final DispatchActivationFlag dispatchActivationFlag;

    @KafkaListener(
            topics = "${message.signal.topic:billing-batch-done}",
            groupId = "billing-message-signal-group",
            containerFactory = "signalKafkaListenerContainerFactory")
    public void onSignal(String payload, Acknowledgment ack) {
        try {
            dispatchActivationFlag.enable();
            MessageListenerContainer container = registry.getListenerContainer("billingMessageListener");
            if (container != null && !container.isRunning()) {
                container.start();
                log.info("[Signal] 메시지 리스너 시작됨: payload={}", payload);
            } else {
                log.info("[Signal] 메시지 리스너 이미 실행 중: payload={}", payload);
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[Signal] 처리 실패", e);
        }
    }
}
