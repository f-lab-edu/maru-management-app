package com.maru.controller.attendance.dto;

import com.maru.domain.attendance.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AttendanceStatusChangeReq(
        @NotNull(message = "변경할 상태는 필수입니다") AttendanceStatus status,
        @Size(max = 500, message = "비고는 500자 이내여야 합니다") String note
) {
}
