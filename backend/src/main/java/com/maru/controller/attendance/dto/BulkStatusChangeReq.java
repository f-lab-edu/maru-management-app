package com.maru.controller.attendance.dto;

import com.maru.domain.attendance.AttendanceStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record BulkStatusChangeReq(
        @NotEmpty(message = "출석 기록 ID 목록은 필수입니다") List<String> attendanceIds,
        @NotNull(message = "변경할 상태는 필수입니다") AttendanceStatus status,
        @Size(max = 500, message = "비고는 500자 이내여야 합니다") String note
) {
}
