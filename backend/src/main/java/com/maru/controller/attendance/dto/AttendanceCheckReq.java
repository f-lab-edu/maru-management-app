package com.maru.controller.attendance.dto;

import com.maru.domain.attendance.AttendanceStatus;
import com.maru.domain.attendance.CheckMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record AttendanceCheckReq(
        @NotNull(message = "원생 ID는 필수입니다") String studentId,
        @NotNull(message = "체크 방법은 필수입니다") CheckMethod method,
        @NotNull(message = "출석 상태는 필수입니다") AttendanceStatus status,
        LocalDate date,
        LocalDateTime checkinAt,
        @Size(max = 500, message = "비고는 500자 이내여야 합니다") String note
) {
}
