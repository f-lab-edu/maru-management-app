package com.maru.controller.guardian.dto;

import lombok.Builder;

@Builder
public record GuardianInfo(
        String name,
        String phone
) {}
