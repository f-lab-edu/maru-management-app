package com.maru.controller.division.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkEnrollmentReq(
        @NotEmpty(message = "원생 ID 목록은 필수입니다")
        List<String> studentIds
) {}
