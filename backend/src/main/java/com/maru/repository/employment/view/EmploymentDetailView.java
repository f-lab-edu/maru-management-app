package com.maru.repository.employment.view;

import com.maru.domain.employment.EmploymentStatus;

import java.time.LocalDateTime;

public interface EmploymentDetailView {
    String getId();
    String getUserId();
    String getUserName();
    String getUserEmail();
    String getUserPhone();
    String getDojangId();
    String getDojangName();
    String getTenantId();
    EmploymentStatus getStatus();
    LocalDateTime getJoinedAt();
    LocalDateTime getEndedAt();
    LocalDateTime getCreatedAt();
}
