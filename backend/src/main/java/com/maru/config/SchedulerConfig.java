package com.maru.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    // 동시 실행 가능한 스케줄 작업 수 (출석: 자동 결석 + 메시지 재시도, 수납: 자동 청구서 발행)
    private static final int SCHEDULER_POOL_SIZE = 3;
    private static final String SCHEDULER_THREAD_PREFIX = "maru-scheduler-";
    // 애플리케이션 종료 시 실행 중인 작업 완료 대기 시간 (초)
    private static final int TERMINATION_WAIT_SECONDS = 60;

    /**
     * TaskScheduler Bean 생성
     *
     * @return TaskScheduler
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(SCHEDULER_POOL_SIZE);
        scheduler.setThreadNamePrefix(SCHEDULER_THREAD_PREFIX);
        scheduler.setAwaitTerminationSeconds(TERMINATION_WAIT_SECONDS);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        return scheduler;
    }
}
