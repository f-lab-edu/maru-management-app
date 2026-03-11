package com.maru.domain;

import com.maru.common.exception.BusinessException;
import com.maru.domain.attendance.Attendance;
import com.maru.domain.attendance.AttendanceStatus;
import com.maru.domain.attendance.exception.AttendanceErrorCode;
import com.maru.fixture.AttendanceFixture;
import com.maru.support.TenantTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttendanceTest {

    @Test
    @DisplayName("출석 생성 시 PRESENT 상태")
    void createAttendance() {
        // given & when
        Attendance attendance = AttendanceFixture.anAttendance().build();

        // then
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("상태 변경")
    void changeStatus() {
        // given
        Attendance attendance = AttendanceFixture.anAttendance().build();

        // when
        attendance.changeStatus(AttendanceStatus.SICK, null);

        // then
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.SICK);
    }

    @Test
    @DisplayName("하원 처리 시 checkoutAt 세팅")
    void checkout() {
        // given
        Attendance attendance = AttendanceFixture.anAttendance().build();
        LocalDateTime checkoutTime = LocalDateTime.of(2026, 2, 19, 18, 0);

        // when
        attendance.checkOut(checkoutTime);

        // then
        assertThat(attendance.getCheckoutAt()).isEqualTo(checkoutTime);
    }

    @Test
    @DisplayName("자동 결석 처리")
    void createAutoAbsent() {
        // given & when
        Attendance attendance = Attendance.createAutoAbsent(
                TenantTestSupport.TEST_TENANT_ID,
                TenantTestSupport.TEST_DOJANG_ID,
                "STU_001",
                LocalDate.of(2026, 2, 19)
        );

        // then
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.ABSENT);
    }

    @Test
    @DisplayName("이미 하원한 출석에 재하원 시 예외")
    void checkoutAlreadyCheckedOut() {
        // given
        Attendance attendance = AttendanceFixture.anAttendance().build();
        attendance.checkOut(LocalDateTime.of(2026, 2, 19, 18, 0));

        // when & then
        assertThatThrownBy(() -> attendance.checkOut(LocalDateTime.of(2026, 2, 19, 19, 0)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.ALREADY_CHECKOUT);
    }

    @Test
    @DisplayName("하원 시각이 입관 시각보다 이전이면 예외")
    void checkoutBeforeCheckin() {
        // given
        Attendance attendance = AttendanceFixture.anAttendance().build();

        // when & then
        assertThatThrownBy(() -> attendance.checkOut(LocalDateTime.of(2026, 2, 19, 14, 0)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.CHECKOUT_BEFORE_CHECKIN);
    }

    @Test
    @DisplayName("동일 상태로 변경 시 예외")
    void changeSameStatus() {
        // given
        Attendance attendance = AttendanceFixture.anAttendance().build();

        // when & then
        assertThatThrownBy(() -> attendance.changeStatus(AttendanceStatus.PRESENT, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.SAME_STATUS);
    }

    @Test
    @DisplayName("하원 기록 없이 취소 시 예외")
    void cancelCheckoutWithoutCheckout() {
        // given
        Attendance attendance = AttendanceFixture.anAttendance().build();

        // when & then
        assertThatThrownBy(() -> attendance.cancelCheckout())
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", AttendanceErrorCode.NOT_CHECKOUT);
    }
}
