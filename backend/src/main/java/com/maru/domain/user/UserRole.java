package com.maru.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    OWNER("관장"),
    INSTRUCTOR("사범");

    private final String description;
}
