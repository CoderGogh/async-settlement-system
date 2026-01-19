package com.touplus.billing_message.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class MessageDispatchConfig {

    @Bean
    public TaskExecutor messageTaskExecutor(
            @Value("${message.dispatch.pool-size:20}") int poolSize,
            @Value("${message.dispatch.queue-size:200}") int queueSize
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(queueSize);
        executor.setThreadNamePrefix("message-dispatch-");
        executor.initialize();
        return executor;
    }
}
