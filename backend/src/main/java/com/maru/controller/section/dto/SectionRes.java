package com.maru.controller.section.dto;

import com.maru.domain.section.Section;
import lombok.Builder;

@Builder
public record SectionRes(
        String id,
        String name,
        Integer displayOrder,
        Integer divisionCount
) {
    public static SectionRes from(Section section, int divisionCount) {
        return SectionRes.builder()
                .id(section.getId())
                .name(section.getName())
                .displayOrder(section.getDisplayOrder())
                .divisionCount(divisionCount)
                .build();
    }
}
