package com.maru.controller.section.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SectionReorderReq(
        @NotEmpty(message = "수련부 ID 목록은 필수입니다")
        List<Long> sectionIds
) {}
