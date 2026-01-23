package com.touplus.billing_batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@EnableScheduling
@Component
@RequiredArgsConstructor
public class BillingKafkaScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("messageJob")
    private final Job messageJob; // 기존 message/batch 코드에서 정의된 Kafka 전송 Job

<<<<<<< HEAD
    @Transactional
    public void sendBillingResult() {
        List<BillingResult> targets = billingResultRepository.findBySendStatusForUpdate(SendStatus.READY);

        for (BillingResult billing : targets) {
            try {
                // Entity → DTO 변환
                BillingResultDto message = new BillingResultDto();
                message.setId(billing.getId());
                message.setSettlementMonth(billing.getSettlementMonth());
                message.setUserId(billing.getUserId());
                message.setTotalPrice(billing.getTotalPrice());
                message.setSettlementDetails(billing.getSettlementDetails());
                message.setSendStatus(billing.getSendStatus());
                message.setBatchExecutionId(billing.getBatchExecutionId());
                message.setProcessedAt(billing.getProcessedAt());
=======
    //    @Scheduled(cron = "0 0 2 2 * ?") // 매월 2일 02시
    public void runBillingKafkaJob() {

        try {
            // Job 파라미터 (중복 실행 방지를 위해 timestamp 추가)
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            // batch Job 실행
            jobLauncher.run(messageJob, params);
>>>>>>> 3c5073978a5336f6c03074d8a2a2ce47cb0036c0

            log.info("[BillingKafkaScheduler] Kafka 전송 Job 실행 완료");

<<<<<<< HEAD
                kafkaTemplate.send(
                        TOPIC,
                        billing.getId().toString(), // Kafka Key
                        message
                ).get(); // 동기 전송

                billing.markSuccess();
                log.info("Kafka 전송 성공 billingResultId={}", billing.getId()); 
            } catch (Exception e) {
                log.error("Kafka 전송 실패 billingResultId={}", billing.getId(), e);
                billing.markFail();
            }
=======
        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn("[BillingKafkaScheduler] 이미 완료된 Kafka 전송 Job: {}", e.getMessage());
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("[BillingKafkaScheduler] Kafka 전송 Job 이미 실행 중: {}", e.getMessage());
        } catch (JobParametersInvalidException e) {
            log.error("[BillingKafkaScheduler] 유효하지 않은 Job 파라미터: {}", e.getMessage());
        } catch (JobRestartException e) {
            log.error("[BillingKafkaScheduler] Job 재시작 실패: {}", e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("[BillingKafkaScheduler] Kafka 전송 Job 실행 실패", e);
>>>>>>> 3c5073978a5336f6c03074d8a2a2ce47cb0036c0
        }
    }
}
