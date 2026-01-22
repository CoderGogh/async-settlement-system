package com.touplus.billing_batch.jobs.message.step;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

@Component
@StepScope // 실행 시점에 JobParameter를 주입받기 위해 필수
@RequiredArgsConstructor
public class TopicCreateTasklet implements Tasklet {

    private final KafkaAdmin kafkaAdmin;
    private static final String BASE_TOPIC = "billing-result-topic-";

    @Value("#{jobParameters['settlementMonth']}") // 실행 시점에 날짜 주입
    private String settlementMonth;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        // [수정] 안전한 토픽명 생성 로직
        String topicSuffix;
        if (settlementMonth.contains("-")) {
            // "2025-12-01" -> "2512"
            topicSuffix = settlementMonth.replace("-", "").substring(2, 6);
        } else {
            // 이미 "2512"로 들어온 경우 그대로 사용
            topicSuffix = settlementMonth;
        }

        String TOPIC = BASE_TOPIC + topicSuffix;

        NewTopic newTopic = TopicBuilder.name(TOPIC)
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(3 * 24 * 60 * 60 * 1000L))
                .build();

        kafkaAdmin.createOrModifyTopics(newTopic);
        return RepeatStatus.FINISHED;
    }
}