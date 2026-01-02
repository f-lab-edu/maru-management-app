package com.maru.controller.groups.dto;

import lombok.Builder;

@Builder
public record GroupDetailRes(
        String id,
        String sectionId,
        String sectionName,
        String name,
        Integer displayOrder,
        Integer studentCount
) {}
