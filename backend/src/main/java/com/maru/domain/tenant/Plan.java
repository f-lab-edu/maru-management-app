package com.maru.domain.tenant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Plan {
    FREE("무료"),
    BASIC("기본"),
    PRO("프로");

    private final String description;
}
