package com.touplus.billing_batch.message;

import com.touplus.billing_batch.BillingBatchApplication;
import com.touplus.DotenvInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(classes = BillingBatchApplication.class)
@ActiveProfiles("local") // .env 파일을 읽고 실제 GCP Kafka(localhost:9092)에 접속함
@ContextConfiguration(initializers = DotenvInitializer.class)   /// ////// 항상 추가하기!!!!!!!!
public class MessageJobRealIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Job messageJob;

    @BeforeEach
    public void setup() {
        // 기존 실행 기록 삭제
        jobRepositoryTestUtils.removeJobExecutions();

        // 테스트 데이터 준비 (DB 상태 초기화)
        jdbcTemplate.execute("DELETE FROM batch_billing_error_log");
        jdbcTemplate.execute("DELETE FROM billing_result");

        // READY 상태의 데이터 5건 삽입
        for (int i = 1; i <= 5; i++) {
            jdbcTemplate.update(
                    "INSERT INTO billing_result (settlement_month, user_id, total_price, settlement_details, send_status, batch_execution_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?)",
                    LocalDate.now().withDayOfMonth(1), (long) i, 10000 * i, "{\"test\":\"data\"}", "READY", 1L
            );
        }
    }

    @Test
    @DisplayName("실제 Kafka 서버 전송 및 DB 상태 업데이트 테스트")
    void realKafkaIntegrationTest() throws Exception {
        // 1. Given
        jobLauncherTestUtils.setJob(messageJob);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        // 2. When (실제 GCP Kafka로 메시지 발송)
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // 비동기 전송 및 DB 반영을 위한 대기
        Thread.sleep(5000);

        // 3. Then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // DB에 SUCCESS로 업데이트 되었는지 확인
        List<Map<String, Object>> results = jdbcTemplate.queryForList("SELECT send_status FROM billing_result");

        System.out.println("### 실제 DB 업데이트 결과 ###");
        results.forEach(System.out::println);

        assertThat(results).isNotEmpty();
        assertThat(results).allSatisfy(row -> {
            String status = (String) row.get("send_status");
            assertThat(status).isEqualTo("SUCCESS");
        });
    }
}