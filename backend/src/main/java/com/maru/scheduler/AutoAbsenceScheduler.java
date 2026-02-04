package com.maru.scheduler;

import com.maru.repository.tenant.DojangSettingRepository;
import com.maru.repository.tenant.view.AutoAbsenceTargetView;
import com.maru.security.TenantContextHolder;
import com.maru.service.attendance.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutoAbsenceScheduler {

    private final DojangSettingRepository dojangSettingRepository;
    private final AttendanceService attendanceService;

    /**
     * 자동 결석 처리 스케줄러
     * 주중 매시간 정각에 해당 시각으로 설정된 도장의 전날 미출석 원생을 결석 처리
     */
    @Scheduled(cron = "0 0 * * * MON-FRI", zone = "Asia/Seoul")
    public void processAutoAbsence() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        int currentHour = LocalTime.now().getHour();
        log.info("자동 결석 처리 시작: date={}, hour={}", yesterday, currentHour);

        List<AutoAbsenceTargetView> targets = dojangSettingRepository
                .findAllAutoAbsenceTargets(currentHour);

        int totalCount = 0;
        int failedCount = 0;

        for (AutoAbsenceTargetView target : targets) {
            try (AutoCloseable ignored = TenantContextHolder.withContext(
                    target.getTenantId(), TenantContextHolder.SYSTEM_USER_ID,
                    target.getDojangId(), TenantContextHolder.ROLE_OWNER)) {
                int count = attendanceService.processAutoAbsenceForDojang(
                        target.getTenantId(), target.getDojangId(), yesterday);
                totalCount += count;
            } catch (Exception e) {
                failedCount++;
                log.error("자동 결석 처리 실패: dojangId={}", target.getDojangId(), e);
            }
        }

        log.info("자동 결석 처리 완료: date={}, 대상 도장={}, 처리={}, 실패={}",
                yesterday, targets.size(), totalCount, failedCount);
    }
}
