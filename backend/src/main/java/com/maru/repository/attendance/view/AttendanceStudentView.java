package com.maru.repository.attendance.view;

import com.maru.domain.attendance.AttendanceStatus;
import com.maru.domain.attendance.CheckMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AttendanceStudentView {
    String getId();
    String getStudentId();
    String getStudentName();
    AttendanceStatus getStatus();
    CheckMethod getMethod();
    LocalDate getAttendanceDate();
    LocalDateTime getCheckinAt();
    LocalDateTime getCheckoutAt();
    String getNote();
    LocalDateTime getCreatedAt();
}
