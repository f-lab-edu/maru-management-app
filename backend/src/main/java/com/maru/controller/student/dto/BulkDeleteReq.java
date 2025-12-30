package com.maru.controller.student.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkDeleteReq(
        @NotEmpty List<String> studentIds
) {
}
