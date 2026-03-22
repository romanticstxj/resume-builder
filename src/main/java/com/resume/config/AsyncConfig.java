package com.resume.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableScheduling
public class AsyncConfig {

    @Bean("aiParsingExecutor")
    public Executor aiParsingExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(2);
        exec.setMaxPoolSize(4);
        exec.setQueueCapacity(100);
        exec.setThreadNamePrefix("ai-parse-");
        exec.initialize();
        return exec;
    }

    @Bean
    public RateLimiter aiRateLimiter() {
        return RateLimiter.create(3.0);
    }
}

