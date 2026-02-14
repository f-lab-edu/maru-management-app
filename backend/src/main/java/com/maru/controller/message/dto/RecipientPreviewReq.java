package com.maru.controller.message.dto;

import com.maru.domain.message.RecipientType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record RecipientPreviewReq(
        @NotNull RecipientType recipientType,
        List<String> sectionIds,
        List<String> divisionIds,
        List<String> studentIds
) {}
