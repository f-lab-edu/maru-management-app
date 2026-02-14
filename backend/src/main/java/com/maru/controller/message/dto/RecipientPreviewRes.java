package com.maru.controller.message.dto;

import lombok.Builder;

@Builder
public record RecipientPreviewRes(
        int totalStudents,
        int totalGuardians,
        int skippedNoGuardian,
        int skippedNoPhone,
        int estimatedMessageCount
) {
    public static RecipientPreviewRes of(int totalStudents, int totalGuardians,
                                          int skippedNoGuardian, int skippedNoPhone) {
        return RecipientPreviewRes.builder()
                .totalStudents(totalStudents)
                .totalGuardians(totalGuardians)
                .skippedNoGuardian(skippedNoGuardian)
                .skippedNoPhone(skippedNoPhone)
                .estimatedMessageCount(totalGuardians)
                .build();
    }
}
