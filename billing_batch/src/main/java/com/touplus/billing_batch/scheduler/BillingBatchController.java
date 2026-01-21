package com.touplus.billing_batch.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/batch")
public class BillingBatchController {

    private final JobLauncher jobLauncher;
    private final Job billingJob;

    public BillingBatchController(
            JobLauncher jobLauncher,
            Job billingJob
    ) {
        this.jobLauncher = jobLauncher;
        this.billingJob = billingJob;
    }

    @GetMapping("/billing")
    public ResponseEntity<String> runBillingJob(
            @RequestParam(required = false) String settlementMonth
    ) throws Exception {

        // settlementMonth 없으면 현재 월 사용 (YYYY-MM)
        if (settlementMonth == null) {
            settlementMonth = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        JobParameters params = new JobParametersBuilder()
                .addString("settlementMonth", settlementMonth)
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(billingJob, params);

        return ResponseEntity.ok(
                "Billing batch started. settlementMonth=" + settlementMonth
        );
    }
}
