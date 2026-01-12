package com.maru.controller.section.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DivisionReorderReq(
        @NotNull(message = "수련부 ID는 필수입니다")
        String sectionId,

        @NotEmpty(message = "수련반 ID 목록은 필수입니다")
        List<String> divisionIds
) {}
