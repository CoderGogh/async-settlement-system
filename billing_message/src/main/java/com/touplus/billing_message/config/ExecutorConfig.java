package com.touplus.billing_message.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorConfig {

    @Bean
    public ExecutorService billingExecutor() {
        // DB I/O 많은 작업 → 4~6 권장
        return Executors.newFixedThreadPool(4);
    }
}

