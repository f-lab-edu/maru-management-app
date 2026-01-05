package com.maru.scheduler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.CronExpression;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AutoAbsenceSchedulerCronTest {

    private static final String CRON_EXPRESSION = "0 0 9 * * MON-FRI";

    @Test
    @DisplayName("Cron 표현식이 평일 09:00에 실행되도록 설정됨")
    void cronExpression_triggersAt9AM() {
        // given
        CronExpression cron = CronExpression.parse(CRON_EXPRESSION);
        LocalDateTime mondayMidnight = LocalDateTime.of(2025, 12, 15, 0, 0, 0); // 월요일

        // when
        LocalDateTime nextExecution = cron.next(mondayMidnight);

        // then
        assertThat(nextExecution).isNotNull();
        assertThat(nextExecution.getHour()).isEqualTo(9);
        assertThat(nextExecution.getMinute()).isEqualTo(0);
        assertThat(nextExecution.getSecond()).isEqualTo(0);
    }

    @Test
    @DisplayName("금요일 09:00 이후에는 월요일 09:00에 실행")
    void cronExpression_skipWeekend() {
        // given
        CronExpression cron = CronExpression.parse(CRON_EXPRESSION);
        LocalDateTime fridayAfterNine = LocalDateTime.of(2025, 12, 19, 10, 0, 0); // 금요일 10시

        // when
        LocalDateTime nextExecution = cron.next(fridayAfterNine);

        // then
        assertThat(nextExecution).isNotNull();
        assertThat(nextExecution.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(nextExecution.getDayOfMonth()).isEqualTo(22); // 다음 월요일
        assertThat(nextExecution.getHour()).isEqualTo(9);
    }

    @Test
    @DisplayName("토요일에는 실행 안 함 - 월요일로 건너뜀")
    void cronExpression_noSaturday() {
        // given
        CronExpression cron = CronExpression.parse(CRON_EXPRESSION);
        LocalDateTime saturday = LocalDateTime.of(2025, 12, 20, 8, 0, 0); // 토요일

        // when
        LocalDateTime nextExecution = cron.next(saturday);

        // then
        assertThat(nextExecution).isNotNull();
        assertThat(nextExecution.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    @DisplayName("일요일에는 실행 안 함 - 월요일로 건너뜀")
    void cronExpression_noSunday() {
        // given
        CronExpression cron = CronExpression.parse(CRON_EXPRESSION);
        LocalDateTime sunday = LocalDateTime.of(2025, 12, 21, 8, 0, 0); // 일요일

        // when
        LocalDateTime nextExecution = cron.next(sunday);

        // then
        assertThat(nextExecution).isNotNull();
        assertThat(nextExecution.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @Test
    @DisplayName("평일에는 매일 실행됨")
    void cronExpression_runsEveryWeekday() {
        // given
        CronExpression cron = CronExpression.parse(CRON_EXPRESSION);
        LocalDateTime monday = LocalDateTime.of(2025, 12, 15, 10, 0, 0); // 월요일 10시

        // when
        LocalDateTime tuesday = cron.next(monday);
        Assertions.assertNotNull(tuesday);
        LocalDateTime wednesday = cron.next(tuesday);
        Assertions.assertNotNull(wednesday);
        LocalDateTime thursday = cron.next(wednesday);
        Assertions.assertNotNull(thursday);
        LocalDateTime friday = cron.next(thursday);

        // then
        assertThat(tuesday.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(wednesday.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(thursday.getDayOfWeek()).isEqualTo(DayOfWeek.THURSDAY);
        Assertions.assertNotNull(friday);
        assertThat(friday.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
    }
}
