package com.maru.controller.division.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollStudentReq(
        @NotNull(message = "원생 ID는 필수입니다")
        String studentId
) {}
