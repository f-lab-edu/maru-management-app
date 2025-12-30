package com.maru.controller.attendance.dto;

import com.maru.domain.attendance.CheckMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record BulkCheckReq(
        @NotEmpty(message = "원생 ID 목록은 필수입니다") List<String> studentIds,
        @NotNull(message = "체크 방법은 필수입니다") CheckMethod method
) {
}
