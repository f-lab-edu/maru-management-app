package com.maru.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmploymentStatus {
    PENDING("대기"),
    ACTIVE("활성"),
    SUSPENDED("정지"),
    REJECTED("거부"),
    LEFT("퇴사");

    private final String description;
}
