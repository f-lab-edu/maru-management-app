package com.maru.repository.attendance.view;

import com.maru.domain.attendance.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface RangeAttendanceView {
    String getStudentId();
    String getStudentName();
    String getStudentPhotoUrl();
    String getAttendanceId();
    AttendanceStatus getStatus();
    LocalDate getAttendanceDate();
    LocalDateTime getCheckinAt();
    LocalDateTime getCheckoutAt();
}
