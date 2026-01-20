package com.touplus.billing_batch.message;

import com.touplus.billing_batch.BillingBatchApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BillingBatchApplication.class)
@ActiveProfiles("local") // 중요: local 설정을 읽어야 localhost:9092로 접속함
public class KafkaConnectionTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @DisplayName("실제 GCP Kafka 서버로 메시지 전송 테스트 (터널링 필수)")
    void realKafkaSendTest() throws Exception {
        // 1. 준비: 보낼 데이터 (billing_message가 받는 형식에 맞춰 수정 가능)
        String topic = "billing-result";
        String message = "{\"userId\": 1, \"totalPrice\": 50000, \"status\": \"TEST_SUCCESS\"}";

        System.out.println(">>> Kafka로 메시지 전송 시작...");

        // 2. 실행: 실제 전송
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, message);

        // 3. 검증: 10초 이내에 응답이 오는지 확인 (GCP 통신 감안)
        SendResult<String, Object> result = future.get(10, TimeUnit.SECONDS);

        // 4. 결과 출력
        System.out.println(">>> 전송 완료!");
        System.out.println(">>> Topic: " + result.getRecordMetadata().topic());
        System.out.println(">>> Partition: " + result.getRecordMetadata().partition());
        System.out.println(">>> Offset: " + result.getRecordMetadata().offset());

        assertThat(result.getRecordMetadata().offset()).isGreaterThanOrEqualTo(0);
    }
}