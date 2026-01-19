package com.maru.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.maru.security.TenantContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 가상 스레드 기반 Executor
     *
     * @return Executor
     */
    @Bean
    public Executor taskExecutor() {
        Executor virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

        return runnable -> {
            TenantContextHolder.ContextInfo context = TenantContextHolder.getContextInfo();

            virtualExecutor.execute(() -> {
                if (context != null) {
                    try (AutoCloseable ignored = TenantContextHolder.withContext(context)) {
                        runnable.run();
                    } catch (Exception e) {
                        throw new RuntimeException("비동기 작업 실행 중 오류 발생", e);
                    }
                } else {
                    runnable.run();
                }
            });
        };
    }
}
