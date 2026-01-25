package com.touplus.billing_batch.jobs.message.step;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class SignalPublishTasklet implements Tasklet {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${message.signal.topic:billing-batch-done}")
    private String signalTopic;

    @Value("#{jobParameters['settlementMonth']}")
    private String settlementMonth;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String payload = "BATCH_DONE:" + settlementMonth + ":" + LocalDateTime.now();
        kafkaTemplate.send(signalTopic, settlementMonth, payload).get();
        log.info("✅ 신호 토픽 발행 완료: topic={}, payload={}", signalTopic, payload);
        return RepeatStatus.FINISHED;
    }
}
