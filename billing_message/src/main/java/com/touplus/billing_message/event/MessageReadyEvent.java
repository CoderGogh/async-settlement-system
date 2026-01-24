package com.touplus.billing_message.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

/**
 * Processor가 Message DB 저장을 완료했을 때 발행되는 이벤트.
 * Scheduler가 이 이벤트를 수신하여 즉시 발송 처리를 시작한다.
 * 
 * billingIds를 전달하여 DB에서 message_id를 조회하도록 한다.
 * (JDBC batchInsert는 생성된 ID를 반환하지 않기 때문)
 */
public class MessageReadyEvent extends ApplicationEvent {

    private final List<Long> billingIds;

    public MessageReadyEvent(Object source, List<Long> billingIds) {
        super(source);
        this.billingIds = billingIds;
    }

    public List<Long> getBillingIds() {
        return billingIds;
    }
}
