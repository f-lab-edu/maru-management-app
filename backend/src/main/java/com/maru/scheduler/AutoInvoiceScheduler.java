package com.maru.scheduler;

import com.maru.service.invoice.AutoInvoiceService;
import com.maru.service.invoice.result.AutoInvoiceResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutoInvoiceScheduler {

    private final AutoInvoiceService autoInvoiceService;

    /**
     * 자동 청구서 발행 스케줄러
     * 매시간 정각에 해당 시각으로 설정된 도장의 활성 원생에게 청구서 자동 생성
     */
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void processAutoInvoice() {
        LocalDate today = LocalDate.now();
        int currentHour = LocalTime.now().getHour();
        log.info("자동 청구서 발행 시작: date={}, hour={}", today, currentHour);

        AutoInvoiceResult result = autoInvoiceService.generateAutoInvoices(today, currentHour);

        log.info("자동 청구서 발행 완료: 대상 도장={}, 생성={}, 스킵={}, 실패={}",
                result.targetDojangCount(), result.createdCount(),
                result.skippedCount(), result.failedDojangCount());
    }
}
