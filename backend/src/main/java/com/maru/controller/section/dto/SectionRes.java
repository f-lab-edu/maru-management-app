package com.maru.controller.section.dto;

import lombok.Builder;

@Builder
public record SectionRes(
        Long id,
        String name,
        Integer displayOrder,
        Integer classCount
) {}
