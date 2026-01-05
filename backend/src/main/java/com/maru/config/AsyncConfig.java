package com.maru.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {

    // TODO: application.yml의 spring.threads.virtual.enabled=true 방식으로 전환할지 고려해보기. 웹 요청 전체가 가상 스레드로 바뀌므로 테스트 필요.

    /**
     * 가상 스레드 기반 Executor
     *
     * @return Executor
     */
    @Bean
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
