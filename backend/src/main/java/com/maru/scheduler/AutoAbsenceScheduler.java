package com.maru.scheduler;

import com.maru.security.TenantContextHolder;
import com.maru.service.attendance.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutoAbsenceScheduler {

    private final AttendanceService attendanceService;

    /**
     * 자동 결석 처리 스케줄러
     * 주중 09:00에 전날 출석 기록이 없는 활성 원생을 결석 처리
     */
    @Scheduled(cron = "0 0 9 * * MON-FRI", zone = "Asia/Seoul")
    public void processAutoAbsence() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("자동 결석 처리 시작: date={}", yesterday);

        try (AutoCloseable ignored = TenantContextHolder.withSystemContext()) {
            int count = attendanceService.processAutoAbsence(yesterday);
            log.info("자동 결석 처리 완료: date={}, count={}", yesterday, count);
        } catch (Exception e) {
            log.error("자동 결석 처리 실패: date={}", yesterday, e);
        }
    }
}
