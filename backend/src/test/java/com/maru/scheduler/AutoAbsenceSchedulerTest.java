package com.maru.scheduler;

import com.maru.service.attendance.AttendanceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AutoAbsenceSchedulerTest {

    @Mock
    AttendanceService attendanceService;

    @InjectMocks
    AutoAbsenceScheduler scheduler;

    @Test
    @DisplayName("자동 결석 처리 시 전날 날짜로 서비스 호출")
    void processAutoAbsence_callsServiceWithYesterday() {
        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        given(attendanceService.processAutoAbsence(yesterday)).willReturn(5);

        // when
        scheduler.processAutoAbsence();

        // then
        then(attendanceService).should().processAutoAbsence(yesterday);
    }

    @Test
    @DisplayName("서비스 예외 발생 시에도 스케줄러는 정상 종료됨")
    void processAutoAbsence_swallowsException() {
        // given
        given(attendanceService.processAutoAbsence(any()))
                .willThrow(new RuntimeException("DB 연결 실패"));

        // when & then
        assertThatCode(() -> scheduler.processAutoAbsence())
                .doesNotThrowAnyException();
    }
}
