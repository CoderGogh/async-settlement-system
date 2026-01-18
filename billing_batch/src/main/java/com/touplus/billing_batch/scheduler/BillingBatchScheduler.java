package com.touplus.billing_batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@EnableScheduling
@Component
@RequiredArgsConstructor
public class BillingBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job billingJob;

//    @Scheduled(cron = "0 0 2 1 * ?") // 매월 1일 02시
    public void runMonthlyBilling() throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addString("settlementMonth", LocalDate.now().withDayOfMonth(1).toString())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(billingJob, params);
    }
}
